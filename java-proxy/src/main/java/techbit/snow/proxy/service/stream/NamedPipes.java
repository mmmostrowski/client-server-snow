package techbit.snow.proxy.service.stream;

import jakarta.annotation.PostConstruct;
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
        final File[] files = pipesDir().toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory() && !file.delete()) {
                    throw new IOException("Cannot delete pipe file: " + file);
                }
            }
        }
    }

    @Bean
    public Path pipesDir() {
        return Path.of(System.getProperty("user.dir") + "/../.pipes/");
    }

}
