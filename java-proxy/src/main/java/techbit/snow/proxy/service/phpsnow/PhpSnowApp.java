package techbit.snow.proxy.service.phpsnow;

import com.google.common.io.Files;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Log4j2
@Service
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class PhpSnowApp {

    private final String sessionId;

    private final PhpSnowConfig config;

    private final String applicationPid;

    private final ProcessBuilder builder;

    private @Nullable Process process;

    public void start() throws IOException {
        stop();

        final Path phpSnowPath = Path.of(Files.simplifyPath(System.getProperty("user.dir") + "/../run"));

        builder.environment().put("SCRIPT_OWNER_PID", applicationPid);
        builder.command(
            Objects.toString(phpSnowPath),
            "server",
            Objects.toString(sessionId),
            Objects.toString(config.getWidth()),
            Objects.toString(config.getHeight()),
            Objects.toString(config.getFps()),
            Objects.toString(config.getDuration().getSeconds()),
            Objects.toString(config.getPresetName())
        );

        String cmd = String.join(" ", builder.command());
        log.debug("start( {} ) | Starting process: {}", sessionId, cmd);
        process = builder.start();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
}
