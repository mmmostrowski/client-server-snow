package techbit.snow.proxy.service.stream;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
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

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Spy
    private NamedPipes namedPipes;


    @Test
    void whenAskedForPipesDir_thenValidDirectoryPathProvided() {
        assertDoesNotThrow(() -> namedPipes.pipesDir().toRealPath());
    }

    @Test
    void whenDestroyingAllPipes_thenFolderWithPipesMustBePurged() throws IOException {
        folder.create();
        folder.newFile("x");
        folder.newFolder("y");
        folder.newFile("z");
        when(namedPipes.pipesDir()).thenReturn(folder.getRoot().toPath());

        namedPipes.destroyAll();

        try (Stream<Path> stream = Files.list(folder.getRoot().toPath())) {
            assertEquals(0, stream.count());
        }
    }

}