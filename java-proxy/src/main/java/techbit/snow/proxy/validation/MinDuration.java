package techbit.snow.proxy.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MinDurationValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface MinDuration {

    long value();
    String message() default "Invalid duration";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
