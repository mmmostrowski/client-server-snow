package techbit.snow.proxy.snow.php;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NamedPipesTest {

    @TempDir
    public Path folder;

    private NamedPipes namedPipes;

    @BeforeEach
    void setup() {
        namedPipes = new NamedPipes(folder);
    }

    @Test
    void whenDestroyingAllPipes_thenFolderWithPipesMustBePurged() throws IOException {
        Assertions.fail();
        Files.createFile(folder.resolve("particlesX"));
        Files.createFile(folder.resolve("particlesY"));
        Files.createFile(folder.resolve("z"));

        namedPipes.destroyAll();

        try (Stream<Path> stream = Files.list(folder)) {
            assertEquals(0, stream.count());
        }
    }

    @Test
    void whenDestroyingAllPipes_thenKeepHiddenFilesInFolderWithPipes() throws IOException {
        Files.createFile(folder.resolve("normal"));
        Files.createFile(folder.resolve(".hidden1"));
        Files.createFile(folder.resolve(".hidden2"));

        namedPipes.destroyAll();

        try (Stream<Path> stream = Files.list(folder)) {
            assertEquals(2, stream.count());
        }
    }

    @Test
    void whenCannotDestroyAnyOfPipes_thenThrowException() throws IOException {
        Files.createFile(folder.resolve("particlesX"));
        Files.createFile(folder.resolve("particlesY"));
        Files.createFile(folder.resolve("z"));
        Assertions.assertTrue(folder.toFile().setWritable(false));

        assertThrows(IOException.class, namedPipes::destroyAll);
    }

    @Test
    void whenNoPipesFolder_thenNoErrorOccurs() {
        if (!folder.toFile().delete()) {
            throw new RuntimeException("Cannot test properly. Need to be able to delete folder!");
        }

        assertDoesNotThrow(namedPipes::destroyAll);
    }

}