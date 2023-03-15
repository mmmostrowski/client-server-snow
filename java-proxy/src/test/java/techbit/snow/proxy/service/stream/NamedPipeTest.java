package techbit.snow.proxy.service.stream;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class NamedPipeTest {

    public TemporaryFolder folder = new TemporaryFolder();

    private NamedPipe namedPipe;

    private Path pipePath;

    @BeforeEach
    void setup() throws IOException {
        folder.create();
        pipePath = folder.newFile("session-xyz").toPath();

        Files.writeString(pipePath, "fake-content");
        namedPipe = new NamedPipe("session-xyz", folder.getRoot().toPath());
    }

    @Test
    void givenPipeFile_whenReadingInputStream_thenProvidesValidContent() throws InterruptedException, IOException {
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
    void givenNoPipeFile_whenReading_thenBlockingUntilIsAvailable() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            void thread1() throws IOException {
                Assertions.assertTrue(pipePath.toFile().delete());
                try(InputStream input = namedPipe.inputStream()) {
                    assertTick(1);
                    Assertions.assertArrayEquals("new-content".getBytes(), input.readAllBytes());
                }
            }

            void thread2() throws IOException {
                waitForTick(1);
                Files.writeString(pipePath, "new-content");
            }
        });
    }

    @Test
    void givenPipeFile_whenCannotDeletePipeFile_thenThrowException() throws IOException {
        Assertions.assertTrue(folder.getRoot().setWritable(false));
        Assertions.assertThrows(Exception.class, () -> namedPipe.destroy());
    }
}