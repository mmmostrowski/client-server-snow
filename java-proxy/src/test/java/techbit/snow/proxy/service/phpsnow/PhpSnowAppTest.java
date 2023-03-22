package techbit.snow.proxy.service.phpsnow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhpSnowAppTest {

    private final PhpSnowConfig config = new PhpSnowConfig(
            "customPreset", 135, 85, Duration.ofMinutes(1), 35);

    @Mock(answer = RETURNS_DEEP_STUBS)
    private ProcessBuilder builder;

    @Mock
    private Process process;

    private PhpSnowApp phpSnowApp;

    @BeforeEach
    void setup() {
        phpSnowApp = new PhpSnowApp("session-abc", config, "98765", builder);
    }

    @Test
    void whenStart_thenValidProcessIsExecuted() throws IOException {
        phpSnowApp.start();

        verify(builder).command(
                argThat(s -> s.endsWith("/run")),
                eq("server"),
                eq("session-abc"),
                eq("135"),
                eq("85"),
                eq("35"),
                eq("60"),
                eq("customPreset")
        );

        verify(builder).start();
    }

    @Test
    void whenStart_thenOurPidIsProvidedInEnvironmentVariable() throws IOException {
        phpSnowApp.start();

        verify(builder.environment()).put("SCRIPT_OWNER_PID", "98765");
    }

    @Test
    void whenDidNotStart_thenIsNotAlive() {
        assertFalse(phpSnowApp.isAlive());
    }

    @Test
    void whenStart_thenIsAlive() throws IOException {
        when(builder.start().isAlive()).thenReturn(true);

        phpSnowApp.start();

        assertTrue(phpSnowApp.isAlive());
    }

    @Test
    void whenStop_thenIsNotAlive() throws IOException {
        when(builder.start().isAlive()).thenReturn(true);

        phpSnowApp.start();
        assertTrue(phpSnowApp.isAlive());

        phpSnowApp.stop();
        assertFalse(phpSnowApp.isAlive());
    }

    @Test
    void whenStop_thenProcessIsDestroyed() throws IOException {
        when(builder.start()).thenReturn(process);

        phpSnowApp.start();
        phpSnowApp.stop();

        verify(process).destroyForcibly();
    }
}