package techbit.snow.proxy.service.stream;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@Component
@SuppressWarnings("unused")
public class NamedPipes {

    @PostConstruct
    public void destroyAll() throws IOException {
        final File[] files = pipesDir().toFile().listFiles();
        if (files == null) {
            return;
        }

        String problematicFiles = stream(files)
                .filter((f) -> !f.isDirectory() && !f.delete())
                .map(File::toString)
                .collect(joining(", "));
        if (!problematicFiles.isEmpty()) {
            throw new IOException("Cannot delete pipe files: " + problematicFiles);
        }
    }

    @Bean
    public Path pipesDir() {
        return Path.of(System.getProperty("user.dir") + "/../.pipes/");
    }

}
