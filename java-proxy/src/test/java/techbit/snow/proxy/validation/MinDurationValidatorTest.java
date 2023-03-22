package techbit.snow.proxy.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinDurationValidatorTest {

    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private MinDuration minDuration;

    private MinDurationValidator validator;

    @BeforeEach
    void setup() {
        when(minDuration.value()).thenReturn(12L);

        validator = new MinDurationValidator();
        validator.initialize(minDuration);
    }

    @Test
    void whenDurationSatisfyMinimalConstraint_thenIsValid() {
        assertTrue(validator.isValid(Duration.ofSeconds(55), context));
    }

    @Test
    void whenDurationIsExactlyAtMinimalConstraint_thenIsValid() {
        assertTrue(validator.isValid(Duration.ofSeconds(12), context));
    }

    @Test
    void whenDurationIsBelowMinimalConstraint_thenIsInvalid() {
        assertFalse(validator.isValid(Duration.ofSeconds(2), context));
    }

}