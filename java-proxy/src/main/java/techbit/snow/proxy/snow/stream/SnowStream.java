package techbit.snow.proxy.snow.stream;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.experimental.StandardException;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import techbit.snow.proxy.config.PhpSnowConfig;
import techbit.snow.proxy.dto.*;
import techbit.snow.proxy.error.IncompatibleConfigException;
import techbit.snow.proxy.snow.php.NamedPipe;
import techbit.snow.proxy.snow.php.PhpSnowApp;
import techbit.snow.proxy.snow.transcoding.StreamDecoder;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;


@Log4j2
@Service
@Scope(SCOPE_PROTOTYPE)
public final class SnowStream {

    @StandardException
    public static class ConsumerThreadException extends Exception { }

    public class SnowStreamFinishedEvent extends ApplicationEvent {

        public SnowStreamFinishedEvent(Object source) {
            super(source);
        }

        public String getSessionId() {
            return sessionId;
        }
    }

    private final NamedPipe pipe;

    private final String sessionId;
    private final PhpSnowApp phpSnowApp;
    private final SnowDataBuffer buffer;
    private final StreamDecoder decoder;
    private final ServerMetadata serverMetadata;
    private final PhpSnowConfig phpSnowConfig;
    private final Semaphore consumerGoingDownLock = new Semaphore(0);
    private final ApplicationEventPublisher applicationEventPublisher;
    private volatile ConsumerThreadException consumerException;
    private volatile boolean destroyed = false;
    private volatile boolean running = false;
    private SnowAnimationMetadata metadata;
    private SnowBackground background;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("snow-stream-consumer-thread-%d").build()
    );

    public SnowStream(String sessionId, PhpSnowConfig phpSnowConfig,
                      ServerMetadata serverMetadata, NamedPipe pipe, PhpSnowApp phpSnowApp,
                      SnowDataBuffer buffer, StreamDecoder decoder, ApplicationEventPublisher applicationEventPublisher
    ) {
        this.sessionId = sessionId;
        this.pipe = pipe;
        this.buffer = buffer;
        this.decoder = decoder;
        this.phpSnowApp = phpSnowApp;
        this.phpSnowConfig = phpSnowConfig;
        this.serverMetadata = serverMetadata;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public boolean isActive() {
        return running && phpSnowApp.isAlive();
    }

    public void startPhpApp() throws IOException {
        if (destroyed) {
            throw new IllegalStateException("You cannot use snow stream twice!");
        }

        pipe.destroy();

        phpSnowApp.start();
    }

    public void startConsumingSnowData() throws IOException {
        if (destroyed) {
            throw new IllegalStateException("You cannot use snow stream twice!");
        }

        if (!phpSnowApp.isAlive()) {
            throw new IllegalStateException("Please startPhpApp() first!");
        }

        log.debug("startConsumingSnowData( {} ) | Opening pipe stream", sessionId);
        final InputStream stream = pipe.inputStream();
        final DataInputStream dataStream = new DataInputStream(stream);

        log.debug("startConsumingSnowData( {} ) | Reading metadata", sessionId);
        metadata = decoder.decodeMetadata(dataStream, serverMetadata, phpSnowConfig.duration());

        log.debug("startConsumingSnowData( {} ) | Reading background", sessionId);
        background = decoder.decodeBackground(dataStream);

        log.debug("startConsumingSnowData( {} ) | Running worker thread", sessionId);
        executor.submit(() -> consumePhpSnowInAThread(stream));
        running = true;
    }

    private void consumePhpSnowInAThread(InputStream stream) {
        try (stream) {
            log.debug("consumeSnowFromPipeThread( {} ) | Start pipe", sessionId);
            final DataInputStream dataStream = new DataInputStream(stream);
            SnowBasis currentBasis = SnowBasis.NONE;
            while (isActive()) {
                final SnowDataFrame frame = decoder.decodeFrame(dataStream);
                if (frame == SnowDataFrame.LAST) {
                    break;
                }
                final SnowBasis basis = decoder.decodeBasis(dataStream);
                if (basis == SnowBasis.NONE) {
                    log.trace("consumeSnowFromPipeThread( {} ) | Frame {}",
                            sessionId, frame.frameNum());
                } else {
                    log.trace("consumeSnowFromPipeThread( {} ) | Frame {} ( with basis update )",
                            sessionId, frame.frameNum());
                    currentBasis = basis;
                }

                buffer.push(frame.withBasis(currentBasis));
            }
            log.trace("consumeSnowFromPipeThread( {} ) | Last Frame", sessionId);
            buffer.push(SnowDataFrame.LAST);
            buffer.waitUntilAllClientsUnregister();
            log.trace("consumeSnowFromPipeThread( {} ) | Stop pipe", sessionId);
        } catch (Throwable e) {
            log.error("consumeSnowFromPipeThread( {} ) | ERROR", sessionId, e);
            consumerException = new ConsumerThreadException(e);
        } finally {
            buffer.destroy();
            disableConsumerThread();
            applicationEventPublisher.publishEvent(createSnowStreamFinishedEvent());
        }
    }

    public void streamTo(SnowStreamClient client)
            throws IOException, InterruptedException, ConsumerThreadException
    {
        throwConsumerExceptionIfAny();

        if (!isActive()) {
            throw new IOException("Stream is not active!");
        }

        log.debug("streamTo( {} ) | Start ({})", sessionId, phpSnowConfig);

        log.debug("streamTo( {} ) | Register To Buffer", sessionId);
        buffer.registerClient(client.identifier());

        try {
            log.debug("streamTo( {} ) | Metadata & Background", sessionId);
            client.startStreaming(metadata, background);

            log.debug("streamTo( {} ) | Start Streaming Frames", sessionId);
            SnowBasis currentBasis = SnowBasis.NONE;
            for (SnowDataFrame frame = buffer.firstFrame(); frame != SnowDataFrame.LAST; frame = buffer.nextFrame(frame)) {
                log.trace("streamTo( {} ) | Frame {}", sessionId, frame.frameNum());

                if (!client.continueStreaming()) {
                    break;
                }

                if (frame.basis() == currentBasis) {
                    client.streamFrame(frame, SnowBasis.NONE);
                } else {
                    client.streamFrame(frame, currentBasis = frame.basis());
                }
            }

            client.streamFrame(SnowDataFrame.LAST, SnowBasis.NONE);

            throwConsumerExceptionIfAny();

            log.debug("streamTo( {} ) | Last frame", sessionId);
            client.stopStreaming();
        } finally {
            log.debug("streamTo( {} ) | Unregister From Buffer", sessionId);
            buffer.unregisterClient(client.identifier());
        }
    }

    private void throwConsumerExceptionIfAny() throws ConsumerThreadException {
        if (consumerException != null) {
            log.debug("streamTo( {} ) | Consumer Exception", sessionId);
            throw consumerException;
        }
    }

    public void waitUntilConsumerThreadFinished() throws InterruptedException {
        if (!running) {
            return;
        }
        consumerGoingDownLock.acquire();
    }

    public void stop() throws IOException, InterruptedException {
        if (running) {
            stopConsumerThread();
        }
        phpSnowApp.stop();
        pipe.destroy();
        destroyed = true;
    }

    public PhpSnowConfig config() {
        return phpSnowConfig;
    }

    public void ensureCompatibleWithConfig(String sessionId, PhpSnowConfig config) {
        if (!phpSnowConfig.equals(config)) {
            throw new IncompatibleConfigException(MessageFormat.format(
                    "Server is already running session ''{0}'' with different configuration. " +
                    "You cannot change configuration when animation is running.", sessionId));
        }
    }

    private void stopConsumerThread() throws InterruptedException {
        running = false;
        if (!consumerGoingDownLock.tryAcquire(2, TimeUnit.SECONDS)) {
            executor.shutdownNow();
            disableConsumerThread();
            buffer.destroy();
        }
    }

    private void disableConsumerThread() {
        running = false;
        consumerGoingDownLock.release(Integer.MAX_VALUE);
    }

    SnowStreamFinishedEvent createSnowStreamFinishedEvent() {
        return new SnowStreamFinishedEvent(this);
    }
}
