package techbit.snow.proxy.service.stream;

import com.google.common.io.MoreFiles;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class NamedPipes {

    @PostConstruct
    public void destroyAll() throws IOException {
        MoreFiles.deleteDirectoryContents(pipesDir());
    }

    public Path pipesDir() {
        return Path.of(System.getProperty("user.dir") + "/../.pipes/");
    }

}
