package techbit.snow.proxy.snow.php;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
@ExtendWith(MockitoExtension.class)
class NamedPipeTest {

    @TempDir
    public Path folder;

    private NamedPipe namedPipe;

    private Path pipePath;

    @BeforeEach
    void setup() throws IOException {
        pipePath = folder.resolve("session-xyz");
        Files.writeString(pipePath, "fake-content");

        namedPipe = new NamedPipe("session-xyz", folder);
    }

    @Test
    void givenPipeFile_whenReadingInputStream_thenProvidesValidContent() throws IOException {
        try (InputStream stream = namedPipe.inputStream()) {
            assertArrayEquals("fake-content".getBytes(), stream.readAllBytes());
        }
    }

    @Test
    void givenPipeFile_whenDestroying_thenPipeFileIsDeleted() throws IOException {
        namedPipe.destroy();

        assertFalse(pipePath.toFile().exists());
    }

    @Test
    void givenNoPipeFile_whenDestroying_thenNoErrorOccurs() {
        Assertions.assertTrue(pipePath.toFile().delete());

        assertDoesNotThrow(namedPipe::destroy);
    }

    @Test
    void givenPipeFile_whenCannotDeletePipeFile_thenThrowException() {
        assertTrue(folder.toFile().setWritable(false));

        assertThrows(IOException.class, namedPipe::destroy);
    }

    @Test
    void givenNoPipeFile_whenReading_thenThrowException() {
        Assertions.assertTrue(pipePath.toFile().delete());

        assertThrows(FileNotFoundException.class, namedPipe::inputStream);
    }

    @Test
    void whenPipeFileExists_thenNamedPipeIsNotMissing() {
        assertFalse(namedPipe.isMissing());
    }

    @Test
    void whenNoPipeFileExists_thenNamedPipeIsMissing() {
        Assertions.assertTrue(pipePath.toFile().delete());

        assertTrue(namedPipe.isMissing());
    }

}