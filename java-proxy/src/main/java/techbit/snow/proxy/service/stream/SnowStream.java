package techbit.snow.proxy.service.stream;

import lombok.experimental.StandardException;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.stream.encoding.StreamDecoder;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Service
@Scope(SCOPE_PROTOTYPE)
@Log4j2
public class SnowStream {

    @StandardException
    public static class ConsumerThreadException extends Exception {
    }

    private final String sessionId;

    private final SnowDataBuffer buffer;

    private final PhpSnowApp phpSnow;

    private final NamedPipe pipe;

    private final PhpSnowConfig phpSnowConfig;

    private final StreamDecoder decoder;

    private final StreamEncoder encoder;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Semaphore consumerLock = new Semaphore(0);

    private SnowAnimationMetadata metadata;

    private boolean running = false;

    private volatile ConsumerThreadException consumerException;

    private boolean destroyed = false;


    public SnowStream(String sessionId, PhpSnowConfig phpSnowConfig,
                      NamedPipe pipe, PhpSnowApp phpSnow, SnowDataBuffer buffer,
                      StreamDecoder decoder, StreamEncoder encoder
    ) {
        this.sessionId = sessionId;
        this.phpSnowConfig = phpSnowConfig;
        this.pipe = pipe;
        this.phpSnow = phpSnow;
        this.buffer = buffer;
        this.decoder = decoder;
        this.encoder = encoder;
    }

    public boolean isActive() {
        return running && phpSnow.isAlive();
    }

    public void startPhpApp() throws IOException {
        if (destroyed) {
            throw new IllegalStateException("You cannot use snow stream twice!");
        }

        pipe.destroy();

        phpSnow.start();
    }

    public void startConsumingSnowData() throws IOException {
        if (destroyed) {
            throw new IllegalStateException("You cannot use snow stream twice!");
        }

        if (!phpSnow.isAlive()) {
            throw new IllegalStateException("Please startPhpApp() first!");
        }

        log.debug("startConsumingSnowData( {} ) | Opening pipe stream", sessionId);
        InputStream stream = pipe.inputStream();

        log.debug("startConsumingSnowData( {} ) | Reading metadata", sessionId);
        metadata = decoder.decodeMetadata(new DataInputStream(stream));

        executor.submit(() -> consumeSnowFromPipeThread(stream));
        running = true;
    }

    public void waitUntilConsumerThreadFinished() throws InterruptedException {
        if (!running) {
            return;
        }
        consumerLock.acquire();
    }

    public void stop() throws IOException, InterruptedException {
        if (running) {
            stopConsumerThread();
        }
        phpSnow.stop();
        pipe.destroy();
        metadata = null;
        destroyed = true;
    }

    private void consumeSnowFromPipeThread(InputStream stream) {
        try (stream) {
            log.debug("consumeSnowFromPipeThread( {} ) | Start pipe", sessionId);
            try (DataInputStream dataStream = new DataInputStream(stream)) {
                while (isActive()) {
                    SnowDataFrame frame = decoder.decodeFrame(dataStream);
                    if (frame == SnowDataFrame.last) {
                        break;
                    }
                    log.trace("consumeSnowFromPipeThread( {} ) | Frame {}", sessionId, frame.frameNum());
                    buffer.push(frame);
                }
                log.trace("consumeSnowFromPipeThread( {} ) | Last Frame", sessionId);
                buffer.push(SnowDataFrame.last);
            }
            log.trace("consumeSnowFromPipeThread( {} ) | Stop pipe", sessionId);
        } catch (InterruptedException ignored) {
        } catch (Throwable e) {
            log.error("consumeSnowFromPipeThread( {} ) | ERROR", sessionId, e);
            consumerException = new ConsumerThreadException(e);
        } finally {
            disableConsumerThread();
            buffer.destroy();
        }
    }

    public void streamTo(OutputStream out) throws IOException, InterruptedException, ConsumerThreadException {
        throwConsumerExceptionIfAny();

        if (!isActive()) {
            throw new IOException("Stream is not active!");
        }

        log.debug("streamTo( {} ) | Metadata", sessionId);

        encoder.encodeMetadata(metadata, out);

        log.debug("streamTo( {} ) | First frame", sessionId);
        for (SnowDataFrame frame = buffer.firstFrame(); frame != SnowDataFrame.last; frame = buffer.nextFrame(frame)) {
            if (frame == SnowDataFrame.empty) {
                continue;
            }
            log.trace("streamTo( {} ) | Frame {}", sessionId, frame.frameNum());

            encoder.encodeFrame(frame, out);
        }

        throwConsumerExceptionIfAny();

        log.debug("streamTo( {} ) | Last frame", sessionId);
        encoder.encodeFrame(SnowDataFrame.last, out);
    }

    private void throwConsumerExceptionIfAny() throws ConsumerThreadException {
        if (consumerException != null) {
            log.debug("streamTo( {} ) | Consumer Exception", sessionId);
            throw consumerException;
        }
    }

    public void ensureCompatibleWithConfig(PhpSnowConfig config) {
        if (!phpSnowConfig.equals(config)) {
            throw new IllegalArgumentException("You cannot change config when animation is running.");
        }
    }

    private void stopConsumerThread() throws InterruptedException {
        running = false;
        if (!consumerLock.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
            executor.shutdownNow();
            disableConsumerThread();
            buffer.destroy();
        }
    }

    private void disableConsumerThread() {
        running = false;
        consumerLock.release();
    }
}
