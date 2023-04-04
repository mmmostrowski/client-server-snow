package techbit.snow.proxy.snow.php;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import techbit.snow.proxy.config.PhpSnowConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static com.google.common.io.Files.simplifyPath;

@Log4j2
@RequiredArgsConstructor
public final class PhpSnowApp {

    private final String sessionId;
    private final PhpSnowConfig config;
    private final String applicationPid;
    private final ProcessBuilder builder;
    private final String bootstrapLocation;
    private @Nullable Process process;

    public void start() throws IOException {
        stop();
        startProcess(
                Path.of(simplifyPath(currentDir() + "/" + bootstrapLocation)),
                "server",
                sessionId,
                config.width(),
                config.height(),
                config.fps(),
                config.durationInSeconds(),
                config.presetName()
        );
    }

    private void startProcess(Object... args) throws IOException {
        final String[] command = Arrays.stream(args)
                .map(Objects::toString)
                .toArray(String[]::new);

        builder.command(command);
        builder.environment().put("SCRIPT_OWNER_PID", applicationPid);

        String cmd = String.join(" ", builder.command());
        log.debug("start( {} ) | Starting process: {}", sessionId, cmd);

        process = builder.start();

        waitAMoment();
        if (!process.isAlive() && process.exitValue() > 0) {
            String stdErr = new String(process.getErrorStream().readAllBytes());
            String stdOut = new String(process.getInputStream().readAllBytes());
            throw new RuntimeException("Error during running '" + cmd + "' :\n" + stdErr + "\n\n" + stdOut);
        }
    }

    public void stop() {
        if (process == null) {
            return;
        }

        log.debug("stop( {} ) | Killing process", sessionId);
        process.destroyForcibly();
        process = null;
    }

    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    @SneakyThrows
    private void waitAMoment()  {
        Thread.sleep(300);
    }

    private static String currentDir() {
        return new File("").getAbsolutePath();
    }
}
