package techbit.snow.proxy.config;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import techbit.snow.proxy.config.validation.MinDuration;

import java.time.Duration;

@Builder
@Jacksonized
public record PhpSnowConfig(

        @Nonnull
        @Size(min = 1, max = 30, message = "Invalid preset name!")
        String presetName,

        @Nullable
        String scene,

        @Min(value = 1, message = "Invalid animation canvas width. Please provide a positive number!")
        int width,

        @Min(value = 1, message = "Invalid animation canvas height. Please provide a positive number!")
        int height,

        @Nonnull
        @MinDuration(value = 1, message = "Invalid animation duration. Please provide a positive number!")
        Duration duration,

        @Min(value = 1, message = "Invalid animation FPS. Please provide a number in between 1 and 60")
        @Max(value = 60, message = "Invalid animation FPS. Please provide a number in between 1 and 60")
        int fps

) {
    public long durationInSeconds() {
        return duration.getSeconds();
    }
}
