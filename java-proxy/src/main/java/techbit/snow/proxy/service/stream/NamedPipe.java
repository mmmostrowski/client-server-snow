package techbit.snow.proxy.service.stream;

import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Service
@Scope(SCOPE_PROTOTYPE)
public class NamedPipe {

    private final File pipeFile;

    public NamedPipe(String sessionId, Path pipesDir) {
        this.pipeFile = pipesDir.resolve(sessionId).toFile();
    }

    public FileInputStream inputStream() throws IOException {
        FileUtils.waitFor(pipeFile, (int) Duration.ofMinutes(30).getSeconds());
        return new FileInputStream(pipeFile);
    }

    public void destroy() throws IOException {
        if (pipeFile.exists() && !pipeFile.delete()) {
            throw new IOException("Cannot delete pipe file: " + pipeFile);
        }
    }

}
