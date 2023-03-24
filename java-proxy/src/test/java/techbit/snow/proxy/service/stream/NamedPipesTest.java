package techbit.snow.proxy.service.stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NamedPipesTest {

    @TempDir
    public Path folder;

    @Spy
    private NamedPipes namedPipes;


    @Test
    void whenAskedForPipesDir_thenValidDirectoryPathProvided() {
        assertDoesNotThrow(() -> namedPipes.pipesDir().toRealPath());
    }

    @Test
    void whenDestroyingAllPipes_thenFolderWithPipesMustBePurged() throws IOException {
        Files.createFile(folder.resolve("x"));
        Files.createFile(folder.resolve("y"));
        Files.createDirectory(folder.resolve("z"));
        when(namedPipes.pipesDir()).thenReturn(folder);

        namedPipes.destroyAll();

        try (Stream<Path> stream = Files.list(folder)) {
            assertEquals(0, stream.count());
        }
    }

}