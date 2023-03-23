package techbit.snow.proxy.service.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.stream.encoding.StreamDecoder;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.nio.file.Path;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnowStreamFactoryTest {

    @Mock
    private ObjectProvider<SnowStream> snowStreamProvider;
    @Mock
    private ObjectProvider<NamedPipe> namedPipesProvider;
    @Mock
    private ObjectProvider<PhpSnowConfig> configProvider;
    @Mock
    private ObjectProvider<PhpSnowApp> phpSnowAppProvider;
    @Mock
    private ObjectProvider<StreamEncoder> streamEncoderProvider;
    @Mock
    private ObjectProvider<StreamDecoder> streamDecoderProvider;
    @Mock
    private ObjectProvider<SnowDataBuffer> snowDataBufferProvider;
    @Mock
    private ObjectProvider<ProcessBuilder> processBuilderProvider;
    @Mock
    private ObjectProvider<BlockingBag<Integer, SnowDataFrame>> blockingBagProvider;
    @Mock
    private NamedPipe namedPipe;
    @Mock
    private PhpSnowApp phpSnowApp;
    @Mock
    private PhpSnowConfig snowConfig;
    @Mock
    private StreamEncoder streamEncoder;
    @Mock
    private StreamDecoder streamDecoder;
    @Mock
    private SnowDataBuffer snowDataBuffer;
    @Mock
    private Map<String, String> configMap;
    @Mock
    private ProcessBuilder processBuilder;
    @Mock
    private BlockingBag<Integer, SnowDataFrame> blockingBag;
    private SnowStreamFactory factory;

    @BeforeEach
    void setup() {
        factory = new SnowStreamFactory(
                mock(Path.class),
                "131",
                12,
                snowStreamProvider, namedPipesProvider,
                phpSnowAppProvider, snowDataBufferProvider,
                processBuilderProvider, blockingBagProvider,
                configProvider, streamDecoderProvider, streamEncoderProvider
        );
    }

    @Test
    void whenSnowStreamIsCreated_thenObjectIsCreatedProperly() {
        when(configProvider.getObject(configMap))
                .thenReturn(snowConfig);
        when(streamDecoderProvider.getObject())
                .thenReturn(streamDecoder);
        when(streamEncoderProvider.getObject())
                .thenReturn(streamEncoder);
        when(blockingBagProvider.getObject())
                .thenReturn(blockingBag);
        when(namedPipesProvider.getObject(eq("session-xyz"), any(Path.class)))
                .thenReturn(namedPipe);
        when(snowDataBufferProvider.getObject(12, blockingBag))
                .thenReturn(snowDataBuffer);
        when(processBuilderProvider.getObject())
                .thenReturn(processBuilder);
        when(phpSnowAppProvider.getObject("session-xyz", snowConfig, "131", processBuilder))
                .thenReturn(phpSnowApp);

        factory.create("session-xyz", configMap);

        verify(snowStreamProvider).getObject("session-xyz",
                snowConfig, namedPipe, phpSnowApp, snowDataBuffer, streamDecoder, streamEncoder);
    }

}