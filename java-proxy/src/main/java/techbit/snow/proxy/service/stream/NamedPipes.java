package techbit.snow.proxy.service.stream;

import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Component
@SuppressWarnings("unused")
public class NamedPipes {

    @PostConstruct
    public void destroyAll() throws IOException {
        final File dir = pipesDir().toFile();
        if (dir.exists()) {
            FileUtils.cleanDirectory(dir);
        }
    }

    @Bean
    public Path pipesDir() {
        return Path.of(System.getProperty("user.dir") + "/../.pipes/");
    }

}
