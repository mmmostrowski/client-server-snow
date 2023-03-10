package techbit.snow.proxy.service.stream;

import com.google.common.io.MoreFiles;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Service
@Scope(SCOPE_PROTOTYPE)
public class NamedPipe {

    private final File pipeFile;

    public NamedPipe(String sessionId) {
        this.pipeFile = pipesDir().resolve(sessionId).toFile();
    }

    public static void destroyAll() throws IOException {
        MoreFiles.deleteDirectoryContents(pipesDir());
    }

    private static Path pipesDir() {
        return Path.of(System.getProperty("user.dir") + "/../.pipes/");
    }

    public FileInputStream inputStream() throws InterruptedException {
        try {
            waitUntilPipeExists();
            return new FileInputStream(pipeFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitUntilPipeExists() throws InterruptedException {
        for (int i = 100; i >= 0 && !pipeFile.exists(); --i) {
            if (i == 0) {
                throw new IllegalStateException("Cannot find file file: " + pipeFile);
            }
            Thread.sleep(150);
        }
    }

    public void destroy() throws IOException {
        if (pipeFile.exists() && !pipeFile.delete()) {
            throw new IOException("Cannot delete pipe file: " + pipeFile);
        }
    }

}
