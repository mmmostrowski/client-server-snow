package techbit.snow.proxy.config.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Duration;

public final class MinDurationValidator implements ConstraintValidator<MinDuration, Duration> {

    private long value;

    @Override
    public void initialize(MinDuration constraintAnnotation) {
        value = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Duration duration, ConstraintValidatorContext context) {
        return duration.getSeconds() >= value;
    }
}
