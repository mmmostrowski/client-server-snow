package techbit.snow.proxy.model;

import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import techbit.snow.proxy.SnowProxyApplication;

import java.io.IOException;
import java.nio.file.Path;

public class PhpSnowApp {

    private final Logger logger = LogManager.getLogger(PhpSnowApp.class);

    private final String sessionId;

    private Process process;

    public PhpSnowApp(String sessionId) {
        this.sessionId = sessionId;
    }

    public void start() throws IOException {
        stop();

        Path phpSnowPath = Path.of(Files.simplifyPath(System.getProperty("user.dir") + "/../run"));

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(phpSnowPath.toString(), "server", sessionId, "180", "70", "33", "300", "massiveSnow");
        builder.environment().put("SCRIPT_OWNER_PID", SnowProxyApplication.pid());

        logger.debug(() -> String.format("start( %s ) | Starting process %s", sessionId, builder.command()));
        process = builder.start();
    }

    public void stop() {
        if (process != null) {
            logger.debug(() -> String.format("stop( %s ) | Killing process", sessionId));
            process.destroyForcibly();
        }
        process = null;
    }

    public boolean isAlive() {
        return process != null && process.isAlive();
    }
}
