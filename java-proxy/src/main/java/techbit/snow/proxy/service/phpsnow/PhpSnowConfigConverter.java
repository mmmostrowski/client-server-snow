package techbit.snow.proxy.service.phpsnow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Maps;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

@Primary
@Component
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class PhpSnowConfigConverter {

    @JsonProperty
    private final String presetName;
    @JsonProperty
    private final int width;
    @JsonProperty
    private final int height;
    @JsonProperty
    private final Duration duration;
    @JsonProperty
    private final int fps;
    private final Validator validator;
    private final ObjectMapper mapper;
    private final Map<String, Object> defaults;

    public PhpSnowConfigConverter(
            @Value("${phpsnow.default.preset-name}") String presetName,
            @Value("${phpsnow.default.width}") int width,
            @Value("${phpsnow.default.height}") int height,
            @Value("#{ ${phpsnow.default.animation-duration} * 1000 }") Duration duration,
            @Value("${phpsnow.default.fps}") int fps,
            Validator validator
    ) {
        this.presetName = presetName;
        this.width = width;
        this.validator = validator;
        this.height = height;
        this.duration = duration;
        this.fps = fps;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.defaults = mapper.convertValue(this, new TypeReference<>() {});
    }

    public PhpSnowConfig fromMap(Map<String, String> config) {
        return validatedConfigOf(objectConvertedFrom(mapMergedWithDefaults(config)));
    }

    public Map<String, Object> toMap(PhpSnowConfig config) {
        return mapper.convertValue(config, new TypeReference<>() {});
    }

    private Map<String, Object> mapMergedWithDefaults(Map<String, String> config) {
        final Map<String, Object> result = Maps.newHashMap(defaults);
        result.putAll(config);
        result.computeIfPresent("duration", (k, v) -> "PT" + v + "S");
        return result;
    }

    private PhpSnowConfig objectConvertedFrom(Map<String, Object> config) {
        return mapper.convertValue(config, PhpSnowConfig.class);
    }

    private PhpSnowConfig validatedConfigOf(PhpSnowConfig snowConfig) {
        final Set<ConstraintViolation<PhpSnowConfig>> issues = validator.validate(snowConfig);
        if (issues.isEmpty()) {
            return snowConfig;
        }
        throw new ConstraintViolationException(issues);
    }

}
