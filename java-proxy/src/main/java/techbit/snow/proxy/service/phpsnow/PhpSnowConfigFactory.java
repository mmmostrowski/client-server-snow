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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
public class PhpSnowConfigFactory {

    @JsonProperty
    private final String presetName;

    @JsonProperty
    private final int width;

    @JsonProperty
    private final int height;

    @JsonProperty
    private final Duration animationDuration;

    @JsonProperty
    private final int fps;

    private final Validator validator;

    public PhpSnowConfigFactory(
            @Value("${phpsnow.default.preset-name}") String presetName,
            @Value("${phpsnow.default.width}") int width,
            @Value("${phpsnow.default.height}") int height,
            @Value("#{ ${phpsnow.default.animation-duration} * 1000 }") Duration animationDuration,
            @Value("${phpsnow.default.fps}") int fps,
            @Autowired Validator validator) {
        this.presetName = presetName;
        this.width = width;
        this.height = height;
        this.animationDuration = animationDuration;
        this.fps = fps;
        this.validator = validator;
    }

    @Bean("phpsnowConfig.create")
    @Scope(SCOPE_PROTOTYPE)
    public PhpSnowConfig create(Map<String, String> config) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        final Map<String, Object> defaults = mapper.convertValue(this, new TypeReference<>() {});
        final Map<String, Object> result = Maps.newHashMap(defaults);

        result.putAll(config);

        if (config.containsKey("animationDuration")) {
            result.put("animationDuration", "PT" + config.get("animationDuration") + "S");
        }

        final PhpSnowConfig snowConfig = mapper.convertValue(result, PhpSnowConfig.class);
        return validatedConfig(snowConfig);
    }

    private PhpSnowConfig validatedConfig(PhpSnowConfig snowConfig) {
        final Set<ConstraintViolation<PhpSnowConfig>> issues = validator.validate(snowConfig);
        if (!issues.isEmpty()) {
            throw new ConstraintViolationException(issues);
        }
        return snowConfig;
    }

}
