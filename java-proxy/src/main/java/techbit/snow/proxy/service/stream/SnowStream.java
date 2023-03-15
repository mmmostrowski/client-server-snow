package techbit.snow.proxy.service.stream;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import techbit.snow.proxy.SnowProxyApplication;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.stream.encoding.StreamDecoder;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Service
@Scope(SCOPE_PROTOTYPE)
@Log4j2
public class SnowStream {

    private final String sessionId;

    private final SnowDataBuffer buffer;

    private final PhpSnowApp phpSnow;

    private final NamedPipe pipe;

    private final PhpSnowConfig phpSnowConfig;

    private final StreamDecoder decoder;

    private final StreamEncoder encoder;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Object consumerLock = new Object();

    private SnowAnimationMetadata metadata;

    private boolean running = false;

    private volatile IOException consumerException;


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
        stop();

        phpSnow.start();
    }

    public void startConsumingSnowData() throws IOException {
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
        synchronized (consumerLock) {
            consumerLock.wait();
        }
    }

    public void stop() throws IOException {
        if (running) {
            stopConsumerThread();
            buffer.destroy();
        }
        phpSnow.stop();
        pipe.destroy();
        metadata = null;
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
        } catch (Throwable e) {
            log.error("consumeSnowFromPipeThread( {} ) | ERROR", sessionId, e);
            consumerException = new IOException(e);
            buffer.destroy();
        } finally {
            disableConsumerThread();
        }
    }

    public void streamTo(OutputStream out) throws IOException, InterruptedException {
        if (!isActive()) {
            throw new IOException("Stream is not active!", consumerException);
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

        if (consumerException != null) {
            log.debug("streamTo( {} ) | Consumer Exception", sessionId);
            throw new IOException("Stream is not active!", consumerException);
        }

        log.debug("streamTo( {} ) | Last frame", sessionId);
        encoder.encodeFrame(SnowDataFrame.last, out);
    }

    public void ensureCompatibleWithConfig(PhpSnowConfig config) {
        if (!phpSnowConfig.equals(config)) {
            throw new IllegalArgumentException("You cannot change config when animation is running.");
        }
    }

    private void stopConsumerThread() {
        try{
            running = false;
            synchronized (consumerLock) {
                consumerLock.wait(500);
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            disableConsumerThread();
        }
    }

    private void disableConsumerThread() {
        running = false;
        synchronized (consumerLock) {
            consumerLock.notifyAll();
        }
    }
}
