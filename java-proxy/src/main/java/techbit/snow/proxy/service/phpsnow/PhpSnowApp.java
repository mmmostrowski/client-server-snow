package techbit.snow.proxy.service.phpsnow;

import com.google.common.io.Files;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

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
        builder.command(phpSnowPath.toString(),
            "server", sessionId,
            Integer.toString(config.getWidth()),
            Integer.toString(config.getHeight()),
            Integer.toString(config.getFps()),
            Long.toString(config.getAnimationDuration().getSeconds()),
            config.getPresetName()
        );

        log.debug("start( {} ) | Starting process {}", sessionId, builder.command());
        process = builder.start();
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
