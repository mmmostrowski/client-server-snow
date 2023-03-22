package techbit.snow.proxy.service.phpsnow;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhpSnowConfigFactoryTest {

    @Mock
    private Validator validator;

    @Mock
    private ConstraintViolation<PhpSnowConfig> issue;

    private PhpSnowConfigFactory factory;

    @BeforeEach
    void setup() {
        factory = new PhpSnowConfigFactory(
                "somePreset", 101, 53, Duration.ofMinutes(3), 23, validator);
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
    void givenInvalidFps_whenCreate_thenThrowException() {
        when(validator.validate(Mockito.any(PhpSnowConfig.class))).thenReturn(Set.of(issue));
        assertThrows(ConstraintViolationException.class, () -> factory.create(Map.of("fps", "0")));
        assertThrows(ConstraintViolationException.class, () -> factory.create(Map.of("fps", "-1")));
    }

    @Test
    void givenInvalidWidth_whenCreate_thenThrowException() {
        when(validator.validate(Mockito.any(PhpSnowConfig.class))).thenReturn(Set.of(issue));
        assertThrows(ConstraintViolationException.class, () -> factory.create(Map.of("width", "0")));
        assertThrows(ConstraintViolationException.class, () -> factory.create(Map.of("width", "-1")));
    }

    @Test
    void givenInvalidHeight_whenCreate_thenThrowException() {
        when(validator.validate(Mockito.any(PhpSnowConfig.class))).thenReturn(Set.of(issue));
        assertThrows(ConstraintViolationException.class, () -> factory.create(Map.of("height", "0")));
        assertThrows(ConstraintViolationException.class, () -> factory.create(Map.of("height", "-1")));
    }

    @Test
    void givenInvalidAnimationDuration_whenCreate_thenThrowException() {
        when(validator.validate(Mockito.any(PhpSnowConfig.class))).thenReturn(Set.of(issue));
        assertThrows(ConstraintViolationException.class, () -> factory.create(Map.of("animationDuration", "0")));
        assertThrows(ConstraintViolationException.class, () -> factory.create(Map.of("animationDuration", "-1")));
    }

    @Test
    void givenValidConfigMap_whenCreate_thenReturnsConfigObject() {
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