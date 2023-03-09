package techbit.snow.proxy.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import techbit.snow.proxy.model.serializable.SnowAnimationMetadata;
import techbit.snow.proxy.model.serializable.SnowDataFrame;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SnowStream {

    private final Logger logger = LogManager.getLogger(SnowStream.class);

    private final String sessionId;

    private final SnowDataBuffer buffer = SnowDataBuffer.ofSize (33);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final PhpSnowApp phpSnow;

    private final NamedPipe pipe;

    private SnowAnimationMetadata metadata;

    private boolean running = false;


    public SnowStream(String sessionId) {
        this.sessionId = sessionId;
        this.pipe = new NamedPipe(sessionId);
        this.phpSnow = new PhpSnowApp(sessionId);
    }

    public void startPhpApp() throws IOException {
        stopPhpApp();

        phpSnow.start();
    }

    public void stopPhpApp() {
        phpSnow.stop();
        pipe.destroy();
        running = false;
        metadata = null;
    }

    public void startConsumingSnowData() throws InterruptedException, IOException {
        logger.debug(() -> String.format("startConsumingSnowData( %s ) | Opening pipe stream", sessionId));
        final FileInputStream stream = pipe.inputStream();

        logger.debug(() -> String.format("startConsumingSnowData( %s ) | Reading metadata", sessionId));
        metadata = new SnowAnimationMetadata(new DataInputStream(stream));

        executor.submit(() -> consumeSnowFromPipeThread(stream));
        running = true;
    }

    private void consumeSnowFromPipeThread(FileInputStream stream) {
        logger.debug(() -> String.format("consumeSnowFromPipeThread( %s ) | Start pipe", sessionId));
        try (stream) {
            try (DataInputStream dataStream = new DataInputStream(stream)) {
                while (isActive()) {
                    SnowDataFrame frame = new SnowDataFrame(dataStream);
                    logger.trace(() -> String.format("consumeSnowFromPipeThread( %s ) | Frame %d", sessionId, frame.frameNum));
                    buffer.push(frame);
                    if (frame.isLast()) {
                        break;
                    }
                }
                buffer.destroy();
            }
            logger.trace(() -> String.format("consumeSnowFromPipeThread( %s ) | Stop pipe", sessionId));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void streamTo(OutputStream out) throws IOException, InterruptedException {
        if (!running) {
            throw new IllegalStateException("You must startPhpApp() first!");
        }

        logger.debug(() -> String.format("streamTo( %s ) | metadata", sessionId));

        out.write(metadata.toString().getBytes(StandardCharsets.UTF_8));
        out.write("\n\n".getBytes(StandardCharsets.UTF_8));

        logger.debug(() -> String.format("streamTo( %s ) | Reading first frame", sessionId));
        SnowDataFrame currentFrame = buffer.firstFrame();
        while(isActive() || currentFrame.isLast()) {
            SnowDataFrame finalCurrentFrame = currentFrame;
            logger.trace(() -> String.format("streamTo( %s ) | Frame %d", sessionId, finalCurrentFrame.frameNum));

            out.write(currentFrame.toString().getBytes(StandardCharsets.UTF_8));
            out.write("\n\n".getBytes(StandardCharsets.UTF_8));

            if (currentFrame.isLast()) {
                break;
            }

            currentFrame = buffer.nextFrame(currentFrame);
        }
    }

    private boolean isActive() {
        return running && phpSnow.isAlive();
    }
}
