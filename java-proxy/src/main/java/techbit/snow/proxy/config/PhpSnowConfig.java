package techbit.snow.proxy.config;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.lang.Integer.parseInt;

public class PhpSnowConfig {

    private String presetName = "massiveSnow";
    private int width = 180;
    private int height = 40;
    private Duration animationDuration = Duration.ofMinutes(5);
    private int fps = 33;



    public PhpSnowConfig(Map<String, String> config) {
        Set<String> unknownKeys = new HashSet<>(config.keySet());

        if (config.containsKey("presetName")) {
            presetName = config.get("presetName");
            unknownKeys.remove("presetName");
        }
        if (config.containsKey("width")) {
            width = parseInt(config.get("width"));
            unknownKeys.remove("width");
        }
        if (config.containsKey("height")) {
            height = parseInt(config.get("height"));
            unknownKeys.remove("height");
        }
        if (config.containsKey("fps")) {
            fps = parseInt(config.get("fps"));
            unknownKeys.remove("fps");
        }
        if (config.containsKey("animationDurationSec")) {
            animationDuration = Duration.ofSeconds(parseInt(config.get("animationDurationSec")));
            unknownKeys.remove("animationDuration");
        }

        if (!unknownKeys.isEmpty()) {
            throw new IllegalArgumentException("Invalid configuration keys found: " + unknownKeys);
        }
    }

    public int fps() {
        return fps;
    }

    public int animationDurationSec() {
        return (int) animationDuration.toSeconds();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public String presetName() {
        return presetName;
    }

    public boolean equalsTo(PhpSnowConfig other) {
        return Objects.equals(fps, other.fps)
                && Objects.equals(animationDuration, other.animationDuration)
                && Objects.equals(presetName, other.presetName)
                && Objects.equals(width, other.width)
                && Objects.equals(height, other.height);
    }

    public boolean isCompatibleWith(Map<String, String> other) {
        return other == null || other.isEmpty() || equalsTo(new PhpSnowConfig(other));
    }

}
