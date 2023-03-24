package techbit.snow.proxy.service.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfigFactory;
import techbit.snow.proxy.service.stream.encoding.BinaryStreamDecoder;
import techbit.snow.proxy.service.stream.encoding.PlainTextStreamEncoder;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnowStreamFactoryTest {

    @Mock
    private PhpSnowConfigFactory configProvider;
    @Mock
    private PlainTextStreamEncoder streamEncoder;
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
    private SnowStreamFactoryImpl factory;

    @BeforeEach
    void setup() {
        factory = spy(new SnowStreamFactoryImpl(pipesDir, 12, "131", configProvider));
    }

    @Test
    void whenSnowStreamIsCreated_thenObjectIsCreatedProperly() {
        when(configProvider.create(configMap)).thenReturn(snowConfig);
        doReturn(streamDecoder).when(factory).createBinaryStreamDecoder();
        doReturn(streamEncoder).when(factory).createPlainTextStreamEncoder();
        doReturn(namedPipe).when(factory).createPipe("session-xyz", pipesDir);
        doReturn(snowDataBuffer).when(factory).createSnowDataBuffer(eq(12), any());
        doReturn(phpSnowApp).when(factory).createPhpSnowApp(
                eq("session-xyz"), eq(snowConfig), eq("131"), any(ProcessBuilder.class));
        doReturn(snowStream).when(factory).createSnowStream(
                "session-xyz", snowConfig, namedPipe, phpSnowApp, snowDataBuffer, streamDecoder, streamEncoder
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