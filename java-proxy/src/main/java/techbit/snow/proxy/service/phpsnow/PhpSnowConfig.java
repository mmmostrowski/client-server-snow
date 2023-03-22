package techbit.snow.proxy.service.phpsnow;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.validation.MinDuration;

import java.time.Duration;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Value
@Builder
@Generated
@Component
@Jacksonized
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class PhpSnowConfig {

    @Nonnull
    @Size(min=1, message = "Invalid preset name!")
    String presetName;

    @Min(value = 1, message = "Invalid animation canvas width. Please provide a positive number!")
    int width;

    @Min(value = 1, message = "Invalid animation canvas height. Please provide a positive number!")
    int height;

    @Nonnull
    @MinDuration(value = 1, message = "Invalid animation duration. Please provide a positive number!")
    Duration animationDuration;

    @Min(value = 1, message = "Invalid animation FPS. Please provide a number in between 1 and 60")
    @Max(value = 60, message = "Invalid animation FPS. Please provide a number in between 1 and 60")
    int fps;

}
