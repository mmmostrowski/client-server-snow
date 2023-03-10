package techbit.snow.proxy.service.stream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;

import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
public class SnowStreamFactory {

    @Value("${phpsnow.buffer-size-in-frames}")
    private int bufferSizeInFrames;

    @Autowired
    @Qualifier("phpsnowConfig.create")
    private ObjectProvider<PhpSnowConfig> configProvider;

    @Bean("snowStream.create")
    @Scope(SCOPE_PROTOTYPE)
    public SnowStream create(String sessionId, Map<String, String> config) {
        return new SnowStream(sessionId, configProvider.getObject(config), bufferSizeInFrames);
    }

}
