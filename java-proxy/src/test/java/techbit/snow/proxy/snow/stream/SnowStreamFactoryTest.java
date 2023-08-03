package techbit.snow.proxy.snow.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import techbit.snow.proxy.config.PhpSnowConfig;
import techbit.snow.proxy.config.PhpSnowConfigConverter;
import techbit.snow.proxy.dto.ServerMetadata;
import techbit.snow.proxy.snow.php.NamedPipe;
import techbit.snow.proxy.snow.php.PhpSnowApp;
import techbit.snow.proxy.snow.transcoding.BinaryStreamDecoder;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnowStreamFactoryTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private PhpSnowConfigConverter configProvider;
    @Mock
    private BinaryStreamDecoder streamDecoder;
    @Mock
    private Map<String, String> configMap;
    @Mock
    private SnowDataBuffer snowDataBuffer;
    @Mock
    private PhpSnowConfig snowConfig;
    @Mock
    private SnowStream snowStream;
    @Mock
    private PhpSnowApp phpSnowApp;
    @Mock
    private NamedPipe namedPipe;
    @Mock
    private Path pipesDir;
    private SnowStreamFactory factory;
    private ServerMetadata serverMetadata;

    @BeforeEach
    void setup() {
        serverMetadata = new ServerMetadata(Duration.ofMillis(1500));
        factory = spy(new SnowStreamFactory(
                1500,
                "somewhere",
                39,
                2,
                eventPublisher,
                configProvider,
                "131",
                pipesDir));
    }

    @Test
    void whenSnowStreamIsCreated_thenObjectIsCreated() {
        when(snowConfig.fps()).thenReturn(22);
        when(configProvider.fromMap(configMap)).thenReturn(snowConfig);
        when(pipesDir.resolve("session-xyz")).thenReturn(mock(Path.class));

        SnowStream result = factory.create("session-xyz", configMap);

        assertNotNull(result);
    }

    @Test
    void whenSnowStreamIsCreated_thenObjectHasProperValues() {
        when(snowConfig.fps()).thenReturn(22);
        when(configProvider.fromMap(configMap)).thenReturn(snowConfig);
        doReturn(streamDecoder).when(factory).createBinaryStreamDecoder();
        doReturn(namedPipe).when(factory).createPipe("session-xyz", pipesDir);
        doReturn(snowDataBuffer).when(factory).createSnowDataBuffer(eq(33), any());
        doReturn(phpSnowApp).when(factory).createPhpSnowApp(
                eq("session-xyz"), eq(snowConfig), eq("131"), any(ProcessBuilder.class));
        doReturn(snowStream).when(factory).createSnowStream(
                "session-xyz", snowConfig, namedPipe, phpSnowApp,
                snowDataBuffer, streamDecoder, serverMetadata, eventPublisher,
                39, 2);

        SnowStream result = factory.create("session-xyz", configMap);

        assertSame(snowStream, result);
    }

}