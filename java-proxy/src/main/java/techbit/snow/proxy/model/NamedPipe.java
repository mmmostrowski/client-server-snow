package techbit.snow.proxy.model;

import com.google.common.io.MoreFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public class NamedPipe {

    private final File pipeFile;

    public NamedPipe(String sessionId)
    {
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
            while(!pipeFile.exists()) {
                Thread.sleep(150);
            }
            return new FileInputStream(pipeFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy() {
        pipeFile.delete();
    }

}
