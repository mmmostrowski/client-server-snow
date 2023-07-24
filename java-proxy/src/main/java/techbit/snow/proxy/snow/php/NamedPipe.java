package techbit.snow.proxy.snow.php;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Service
@Scope(SCOPE_PROTOTYPE)
public final class NamedPipe {

    private final File pipeFile;

    public NamedPipe(String sessionId, Path pipesDir) {
        pipeFile = pipesDir.resolve(sessionId).toFile();
    }

    public boolean isMissing() {
        return !pipeFile.exists();
    }

    public InputStream inputStream() throws IOException {
        if (isMissing()) {
            throw new FileNotFoundException("File not found: " + pipeFile.getAbsolutePath());
        }
        return new FileInputStream(pipeFile);
    }

    public void destroy() throws IOException {
        if (pipeFile.exists() && !pipeFile.delete()) {
            throw new IOException("Cannot delete pipe file: " + pipeFile);
        }
    }

}
