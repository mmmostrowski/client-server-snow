package techbit.snow.proxy.model;

import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import techbit.snow.proxy.model.serializable.SnowAnimationMetadata;
import techbit.snow.proxy.model.serializable.SnowDataFrame;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor(staticName = "of")
public class SnowStream {

    private final String sessionId;

    private boolean started = false;

    private PhpSnowApp phpSnow;

    private Process process;

    private final SnowDataBuffer buffer = SnowDataBuffer.ofSize (33);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private SnowAnimationMetadata metadata;

    public void startPhpApp() throws IOException {
        Path phpSnowPath = Path.of(Files.simplifyPath(System.getProperty("user.dir") + "/../run"));

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(phpSnowPath.toString(), "server", sessionId, "180", "70", "massiveSnow");

        process = builder.start();

        started = true;
    }

    public void startConsumingSnowData() throws InterruptedException, IOException {
        File pipe = new File(System.getProperty("user.dir") + "/../.pipes/" + sessionId);

        while(!pipe.exists()) {
            Thread.sleep(150);
        }

        FileInputStream stream = new FileInputStream(pipe);
        metadata = new SnowAnimationMetadata(new DataInputStream(stream));

        executor.submit(() -> {
            try (DataInputStream dataStream = new DataInputStream(stream)) {
                while (process.isAlive()) {
                    buffer.push(new SnowDataFrame(dataStream));
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void stopPhpApp() {
        process.destroy();
    }

    public void streamTo(OutputStream out) throws IOException, InterruptedException {
        if (!started) {
            throw new IllegalStateException("You must startPhpApp() first!");
        }

        out.write(metadata.toString().getBytes(StandardCharsets.UTF_8));
        out.write("\n\n".getBytes(StandardCharsets.UTF_8));

        SnowDataFrame currentFrame = buffer.firstFrame();
        while(process.isAlive()) {
            out.write(currentFrame.toString().getBytes(StandardCharsets.UTF_8));
            out.write("\n\n".getBytes(StandardCharsets.UTF_8));

            currentFrame = buffer.nextFrame(currentFrame);
        }
    }
}
