package techbit.snow.proxy.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
public class SnowStreamFactory {

    @Value("${phpsnow.buffer-size-in-frames}")
    private int bufferSizeInFrames;

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public SnowStream create(String sessionId, Map<String, String> config) {
        return new SnowStream(sessionId, config, bufferSizeInFrames);
    }

}
