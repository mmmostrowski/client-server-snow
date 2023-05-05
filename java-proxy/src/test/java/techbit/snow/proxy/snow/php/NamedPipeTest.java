package techbit.snow.proxy.snow.php;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;

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
        try(InputStream stream = namedPipe.inputStream()) {
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
    void givenNoPipeFile_whenReading_thenBlockingUntilIsAvailable() throws Throwable {
        Assertions.assertTrue(pipePath.toFile().delete());
        TestFramework.runOnce(new MultithreadedTestCase() {
            void thread1() throws IOException {
                waitForTick(1);
                Files.writeString(pipePath, "new-content");
            }

            void thread2() throws IOException {
                try(InputStream input = namedPipe.inputStream()) {
                    assertTick(1);
                    Assertions.assertArrayEquals("new-content".getBytes(), input.readAllBytes());
                }
            }
        });
    }

    @Test
    void givenNoPipeFile_whenWaitingForItTooLong_thenThrowException() {
        Assertions.assertTrue(pipePath.toFile().delete());

        try(MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)) {
            fileUtils.when(() -> FileUtils.waitFor(any(), anyInt())).thenReturn(false);

            assertThrows(IllegalStateException.class, namedPipe::inputStream);
        }
    }
}