package techbit.snow.proxy.service.stream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;

import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
public class SnowStreamFactory {

    private final int bufferSizeInFrames;

    private final ObjectProvider<PhpSnowConfig> configProvider;

    private final String applicationPid;

    private final NamedPipes pipes;


    public SnowStreamFactory(
            @Value("${phpsnow.buffer-size-in-frames}") int bufferSizeInFrames,
            @Autowired @Qualifier("phpsnowConfig.create") ObjectProvider<PhpSnowConfig> configProvider,
            @Autowired @Qualifier("application.pid") String applicationPid,
            @Autowired NamedPipes pipes
    ) {
        this.bufferSizeInFrames = bufferSizeInFrames;
        this.configProvider = configProvider;
        this.applicationPid = applicationPid;
        this.pipes = pipes;
    }

    @Bean("snowStream.create")
    @Scope(SCOPE_PROTOTYPE)
    public SnowStream create(String sessionId, Map<String, String> config) {
        PhpSnowConfig phpSnowConfig = configProvider.getObject(config);
        return new SnowStream(sessionId,
                phpSnowConfig,
                new NamedPipe(sessionId, pipes.pipesDir()),
                new PhpSnowApp(sessionId, phpSnowConfig, applicationPid, new ProcessBuilder()),
                new SnowDataBuffer(bufferSizeInFrames)
        );
    }

}
