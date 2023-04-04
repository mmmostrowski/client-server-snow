package techbit.snow.proxy.service.stream.snow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfigConverter;
import techbit.snow.proxy.service.stream.NamedPipe;
import techbit.snow.proxy.service.stream.encoding.BinaryStreamDecoder;

import java.nio.file.Path;
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

    @BeforeEach
    void setup() {
        factory = Mockito.spy(new SnowStreamFactory(
                12,
                "somewhere",
                eventPublisher,
                configProvider,
                "131",
                pipesDir));
    }

    @Test
    void whenSnowStreamIsCreated_thenObjectIsCreatedProperly() {
        when(configProvider.fromMap(configMap)).thenReturn(snowConfig);
        doReturn(streamDecoder).when(factory).createBinaryStreamDecoder();
        doReturn(namedPipe).when(factory).createPipe("session-xyz", pipesDir);
        doReturn(snowDataBuffer).when(factory).createSnowDataBuffer(eq(12), any());
        doReturn(phpSnowApp).when(factory).createPhpSnowApp(
                eq("session-xyz"), eq(snowConfig), eq("131"), any(ProcessBuilder.class));
        doReturn(snowStream).when(factory).createSnowStream(
                "session-xyz", snowConfig, namedPipe, phpSnowApp, snowDataBuffer, streamDecoder, eventPublisher
        );

        SnowStream result = factory.create("session-xyz", configMap);

        assertSame(snowStream, result);
    }

    @Test
    void whenSnowStreamIsCreated_thenObjectIsCreated() {
        when(pipesDir.resolve("session-xyz")).thenReturn(mock(Path.class));

        SnowStream result = factory.create("session-xyz", configMap);

        assertNotNull(result);
    }

}