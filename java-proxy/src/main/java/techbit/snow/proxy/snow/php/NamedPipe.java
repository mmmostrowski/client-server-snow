package techbit.snow.proxy.snow.php;

import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Service
@Scope(SCOPE_PROTOTYPE)
public class NamedPipe {

    private final File pipeFile;

    public NamedPipe(String sessionId, Path pipesDir) {
        pipeFile = pipesDir.resolve(sessionId).toFile();
    }

    public InputStream inputStream() throws IOException {
        if (!FileUtils.waitFor(pipeFile, (int) Duration.ofMinutes(2).getSeconds())) {
            throw new IllegalStateException("PhpSnow did not create a pipe file: " + pipeFile);
        }
        return new FileInputStream(pipeFile);
    }

    public void destroy() throws IOException {
        if (pipeFile.exists() && !pipeFile.delete()) {
            throw new IOException("Cannot delete pipe file: " + pipeFile);
        }
    }

}
