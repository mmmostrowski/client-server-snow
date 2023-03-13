package techbit.snow.proxy.service.phpsnow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
public class PhpSnowConfigFactory {

    @JsonProperty
    @Value("${phpsnow.default.preset-name}")
    private final String presetName = "massiveSnow";

    @JsonProperty
    @Value("${phpsnow.default.width}")
    private final int width = 180;

    @JsonProperty
    @Value("${phpsnow.default.height}")
    private final int height = 30;

    @JsonProperty
    @Value("#{ ${phpsnow.default.animation-duration} * 1000 }")
    private final Duration animationDuration = Duration.ofMinutes(5);

    @JsonProperty
    @Value("${phpsnow.default.fps}")
    private final int fps = 33;


    @Bean("phpsnowConfig.create")
    @Scope(SCOPE_PROTOTYPE)
    public PhpSnowConfig create(Map<String, String> config) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        Map<String, Object> defaults = mapper.convertValue(this, new TypeReference<>() {});
        Map<String, Object> result = Maps.newHashMap(defaults);

        result.putAll(config);

        return mapper.convertValue(result, PhpSnowConfig.class);
    }

}
