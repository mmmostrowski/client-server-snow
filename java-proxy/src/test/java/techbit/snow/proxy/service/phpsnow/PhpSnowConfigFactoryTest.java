package techbit.snow.proxy.service.phpsnow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PhpSnowConfigFactoryTest {

    private PhpSnowConfigFactory factory;

    @BeforeEach
    void setup() {
        factory = new PhpSnowConfigFactory(
                "somePreset", 101, 53, Duration.ofMinutes(3), 23);
    }

    @Test
    void givenEmptyConfigMap_whenCreate_thenReturnsConfigObjectWithDefaults() {
        PhpSnowConfig config = factory.create(Collections.emptyMap());

        assertEquals("somePreset", config.getPresetName());
        assertEquals(101, config.getWidth());
        assertEquals(53, config.getHeight());
        assertEquals(Duration.ofMinutes(3), config.getAnimationDuration());
        assertEquals(23, config.getFps());
    }

    @Test
    void givenPresetName_whenCreate_thenReturnsValidConfigObject() {
        PhpSnowConfig config = factory.create(Map.of("presetName", "redefinedPresetName"));

        assertEquals("redefinedPresetName", config.getPresetName());
    }

    @Test
    void givenFps_whenCreate_thenReturnsValidConfigObject() {
        PhpSnowConfig config = factory.create(Map.of("fps", "13"));

        assertEquals(13, config.getFps());
    }

    @Test
    void givenWidth_whenCreate_thenReturnsValidConfigObject() {
        PhpSnowConfig config = factory.create(Map.of("width", "113"));

        assertEquals(113, config.getWidth());
    }

    @Test
    void givenHeight_whenCreate_thenReturnsValidConfigObject() {
        PhpSnowConfig config = factory.create(Map.of("height", "73"));

        assertEquals(73, config.getHeight());
    }

    @Test
    void givenAnimationDuration_whenCreate_thenReturnsValidConfigObject() {
        PhpSnowConfig config = factory.create(Map.of("animationDuration", "5940"));

        assertEquals(Duration.ofMinutes(99), config.getAnimationDuration());
    }

    @Test
    void givenConfigMap_whenCreate_thenReturnsValidConfigObject() {
        PhpSnowConfig config = factory.create(Map.of(
            "presetName", "redefinedPresetName",
                "fps", "13",
                "width", "113",
                "height", "73",
                "animationDuration", "5940"
        ));

        assertEquals("redefinedPresetName", config.getPresetName());
        assertEquals(113, config.getWidth());
        assertEquals(73, config.getHeight());
        assertEquals(Duration.ofMinutes(99), config.getAnimationDuration());
        assertEquals(13, config.getFps());
    }

}