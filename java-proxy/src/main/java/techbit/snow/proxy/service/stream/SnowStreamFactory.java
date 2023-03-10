package techbit.snow.proxy.service.stream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Service
public class SnowStreamFactory {

    @Value("${phpsnow.buffer-size-in-frames}")
    private int bufferSizeInFrames;

    @Bean("snowStream.create")
    @Scope(SCOPE_PROTOTYPE)
    public SnowStream create(String sessionId, Map<String, String> config) {
        return new SnowStream(sessionId, config, bufferSizeInFrames);
    }

}
