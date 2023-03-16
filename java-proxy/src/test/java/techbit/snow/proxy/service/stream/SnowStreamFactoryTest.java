package techbit.snow.proxy.service.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;

import java.io.File;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnowStreamFactoryTest {

    @Mock
    private ObjectProvider<PhpSnowConfig> configProvider;

    private SnowStreamFactory factory;

    private PhpSnowConfig mockSnowConfig;

    @BeforeEach
    void setup() {
        factory = new SnowStreamFactory(
                12, configProvider, "131", new NamedPipes());
        mockSnowConfig = new PhpSnowConfig(
                "preset-name", 90, 30, Duration.ofMinutes(1), 22
        );
    }

    @Test
    void whenSnowStreamIsCreated_thenObjectIsCreatedProperly() {
        Map<String, String> configMap = Map.of();
        when(configProvider.getObject(configMap)).thenReturn(mockSnowConfig);

        SnowStream snowStream = factory.create("session-xyz", configMap);

        NamedPipe pipe = privateFieldOf(snowStream, "pipe", NamedPipe.class);
        PhpSnowApp phpSnowApp = privateFieldOf(snowStream, "phpSnow", PhpSnowApp.class);
        SnowDataBuffer buffer = privateFieldOf(snowStream, "buffer", SnowDataBuffer.class);


        assertEquals("session-xyz", privateFieldOf(snowStream, "sessionId", String.class));
        assertEquals("session-xyz", privateFieldOf(phpSnowApp, "sessionId", String.class));
        assertEquals(12, privateFieldOf(buffer, "maxNumOfFrames", Integer.class));
        assertEquals(mockSnowConfig, privateFieldOf(snowStream, "phpSnowConfig", PhpSnowConfig.class));
        assertEquals(mockSnowConfig, privateFieldOf(phpSnowApp, "config", PhpSnowConfig.class));
        assertTrue(privateFieldOf(pipe, "pipeFile", File.class).toString().contains("session-xyz"));
    }

    @SuppressWarnings("unchecked")
    private <T> T privateFieldOf(Object object, String fieldName, Class<T> type) {
        return (T) ReflectionTestUtils.getField(object, fieldName);
    }

}