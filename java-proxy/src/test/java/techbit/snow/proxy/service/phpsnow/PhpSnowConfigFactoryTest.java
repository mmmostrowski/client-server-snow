package techbit.snow.proxy.service.phpsnow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

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

        Assertions.assertEquals("somePreset", config.getPresetName());
        Assertions.assertEquals(101, config.getWidth());
        Assertions.assertEquals(53, config.getHeight());
        Assertions.assertEquals(Duration.ofMinutes(3), config.getAnimationDuration());
        Assertions.assertEquals(23, config.getFps());
    }

    @Test
    void givenPresetName_whenCreate_thenReturnsValidConfigObject() {
        PhpSnowConfig config = factory.create(Map.of("presetName", "redefinedPresetName"));

        Assertions.assertEquals("redefinedPresetName", config.getPresetName());
    }

    @Test
    void givenFps_whenCreate_thenReturnsValidConfigObject() {
        PhpSnowConfig config = factory.create(Map.of("fps", "13"));

        Assertions.assertEquals(13, config.getFps());
    }

    @Test
    void givenWidth_whenCreate_thenReturnsValidConfigObject() {
        PhpSnowConfig config = factory.create(Map.of("width", "113"));

        Assertions.assertEquals(113, config.getWidth());
    }

    @Test
    void givenHeight_whenCreate_thenReturnsValidConfigObject() {
        PhpSnowConfig config = factory.create(Map.of("height", "73"));

        Assertions.assertEquals(73, config.getHeight());
    }

    @Test
    void givenAnimationDuration_whenCreate_thenReturnsValidConfigObject() {
        PhpSnowConfig config = factory.create(Map.of("animationDuration", "5940"));

        Assertions.assertEquals(Duration.ofMinutes(99), config.getAnimationDuration());
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

        Assertions.assertEquals("redefinedPresetName", config.getPresetName());
        Assertions.assertEquals(113, config.getWidth());
        Assertions.assertEquals(73, config.getHeight());
        Assertions.assertEquals(Duration.ofMinutes(99), config.getAnimationDuration());
        Assertions.assertEquals(13, config.getFps());
    }

}