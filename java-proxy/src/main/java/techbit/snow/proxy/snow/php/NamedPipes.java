package techbit.snow.proxy.snow.php;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@Component
public final class NamedPipes {

    private final Path pipesDir;

    public NamedPipes(Path pipesDir) {
        this.pipesDir = pipesDir;
    }

    @PostConstruct
    public void destroyAll() throws IOException {
        final File[] files = pipesDir.toFile().listFiles();
        if (files == null) {
            return;
        }

        String problematicFiles = stream(files)
                .filter(f -> !f.delete())
                .map(File::toString)
                .collect(joining(", "));
        if (!problematicFiles.isEmpty()) {
            throw new IOException("Cannot delete pipe files: " + problematicFiles);
        }
    }

}
