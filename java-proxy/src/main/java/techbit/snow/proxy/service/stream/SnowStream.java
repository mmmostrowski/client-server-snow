package techbit.snow.proxy.service.stream;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jakarta.annotation.Nullable;
import lombok.experimental.StandardException;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.exception.IncompatibleConfigException;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfigConverter;
import techbit.snow.proxy.service.stream.encoding.StreamDecoder;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Log4j2
@Service
@Scope(SCOPE_PROTOTYPE)
public class SnowStream {

    @StandardException
    public static class ConsumerThreadException extends Exception { }

    public interface Customizations {
        default void onMetadataEncoded(SnowAnimationMetadata metadata) {}
        default void onFrameEncoded(SnowDataFrame frame) {}
        default boolean isStreamActive() { return true; }
    }

    public class SnowStreamFinishedEvent extends ApplicationEvent {
        public SnowStreamFinishedEvent(Object source) {
            super(source);
        }

        public String getSessionId() {
            return sessionId;
        }
    }

    private final String sessionId;
    private final PhpSnowApp phpSnowApp;
    private final PhpSnowConfig phpSnowConfig;
    private final SnowDataBuffer buffer;
    private final NamedPipe pipe;
    private final StreamDecoder decoder;
    private final Semaphore consumerGoingDownLock = new Semaphore(0);
    private final ApplicationEventPublisher applicationEventPublisher;
    private volatile boolean running = false;
    private volatile boolean destroyed = false;
    private volatile ConsumerThreadException consumerException;
    private @Nullable SnowAnimationMetadata metadata;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("snow-stream-consumer-thread-%d").build()
    );


    public SnowStream(String sessionId, PhpSnowConfig phpSnowConfig,
                      NamedPipe pipe, PhpSnowApp phpSnowApp, SnowDataBuffer buffer,
                      StreamDecoder decoder, ApplicationEventPublisher applicationEventPublisher
    ) {
        this.sessionId = sessionId;
        this.phpSnowConfig = phpSnowConfig;
        this.pipe = pipe;
        this.phpSnowApp = phpSnowApp;
        this.buffer = buffer;
        this.decoder = decoder;
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
        InputStream stream = pipe.inputStream();

        log.debug("startConsumingSnowData( {} ) | Reading metadata", sessionId);
        metadata = decoder.decodeMetadata(new DataInputStream(stream));

        executor.submit(() -> consumeSnowFromPhpInThread(stream));
        running = true;
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
        metadata = null;
        destroyed = true;
    }

    private void consumeSnowFromPhpInThread(InputStream stream) {
        try (stream) {
            log.debug("consumeSnowFromPipeThread( {} ) | Start pipe", sessionId);
            try (final DataInputStream dataStream = new DataInputStream(stream)) {
                while (isActive()) {
                    final SnowDataFrame frame = decoder.decodeFrame(dataStream);
                    if (frame == SnowDataFrame.LAST) {
                        break;
                    }
                    log.trace("consumeSnowFromPipeThread( {} ) | Frame {}", sessionId, frame.frameNum());
                    buffer.push(frame);
                }
                log.trace("consumeSnowFromPipeThread( {} ) | Last Frame", sessionId);
                buffer.push(SnowDataFrame.LAST);
                buffer.waitUntilAllClientsUnregister();
            }
            log.trace("consumeSnowFromPipeThread( {} ) | Stop pipe", sessionId);
//        } catch (InterruptedException ignored) {
        } catch (Throwable e) {
            log.error("consumeSnowFromPipeThread( {} ) | ERROR", sessionId, e);
            consumerException = new ConsumerThreadException(e);
        } finally {
            buffer.destroy();
            disableConsumerThread();
            applicationEventPublisher.publishEvent(new SnowStreamFinishedEvent(this));
        }
    }

    public void streamTo(OutputStream out, StreamEncoder encoder)
            throws IOException, InterruptedException, ConsumerThreadException
    {
        streamTo(out, encoder, new Customizations() {} );
    }

    public void streamTo(OutputStream out, StreamEncoder encoder, Customizations customs)
            throws IOException, InterruptedException, ConsumerThreadException
    {
        throwConsumerExceptionIfAny();

        if (!isActive()) {
            throw new IOException("Stream is not active!");
        }

        log.debug("streamTo( {} ) | Start ({})", sessionId, phpSnowConfig);

        final Object clientIdentifier = Thread.currentThread();

        log.debug("streamTo( {} ) | Register To Buffer", sessionId);
        buffer.registerClient(clientIdentifier);

        try {
            log.debug("streamTo( {} ) | Metadata", sessionId);
            encoder.encodeMetadata(metadata, out);
            customs.onMetadataEncoded(metadata);

            log.debug("streamTo( {} ) | Reading Frames", sessionId);
            for (SnowDataFrame frame = buffer.firstFrame(); frame != SnowDataFrame.LAST; frame = buffer.nextFrame(frame)) {
                log.trace("streamTo( {} ) | Frame {}", sessionId, frame.frameNum());

                if (!customs.isStreamActive()) {
                    break;
                }

                encoder.encodeFrame(frame, out);
                customs.onFrameEncoded(frame);
            }

            throwConsumerExceptionIfAny();

            log.debug("streamTo( {} ) | Last frame", sessionId);
            encoder.encodeFrame(SnowDataFrame.LAST, out);
        } finally {
            log.debug("streamTo( {} ) | Unregister From Buffer", sessionId);
            buffer.unregisterClient(clientIdentifier);
        }
    }

    private void throwConsumerExceptionIfAny() throws ConsumerThreadException {
        if (consumerException != null) {
            log.debug("streamTo( {} ) | Consumer Exception", sessionId);
            throw consumerException;
        }
    }

    public void ensureCompatibleWithConfig(PhpSnowConfig config) {
        if (!phpSnowConfig.equals(config)) {
            throw new IncompatibleConfigException("You cannot change config when animation is running.");
        }
    }

    public Map<String, Object> configDetails(PhpSnowConfigConverter configConverter) {
        return configConverter.toMap(phpSnowConfig);
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
}
