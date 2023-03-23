package techbit.snow.proxy.service.stream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.stream.encoding.StreamDecoder;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.nio.file.Path;
import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
public class SnowStreamFactory {

    private final ObjectProvider<SnowStream> snowStreamProvider;
    private final ObjectProvider<PhpSnowConfig> configProvider;
    private final ObjectProvider<NamedPipe> namedPipesProvider;
    private final ObjectProvider<PhpSnowApp> phpSnowAppsProvider;
    private final ObjectProvider<SnowDataBuffer> snowDataBufferProvider;
    private final ObjectProvider<StreamDecoder> streamDecoderProvider;
    private final ObjectProvider<StreamEncoder> streamEncoderProvider;
    private final ObjectProvider<ProcessBuilder> processBuilderProvider;
    private final ObjectProvider<BlockingBag<Integer, SnowDataFrame>> blockingBagProvider;
    private final int bufferSizeInFrames;
    private final String applicationPid;
    private final Path namedPipesDir;


    public SnowStreamFactory(
            @Autowired @Qualifier("namedPipes.dir") Path namedPipesDir,
            @Autowired @Qualifier("application.pid") String applicationPid,
            @Value("${phpsnow.buffer-size-in-frames}") int bufferSizeInFrames,
            @Autowired ObjectProvider<SnowStream> snowStreamProvider,
            @Autowired ObjectProvider<NamedPipe> namedPipesProvider,
            @Autowired ObjectProvider<PhpSnowApp> phpSnowAppsProvider,
            @Autowired ObjectProvider<SnowDataBuffer> snowDataBufferProvider,
            @Autowired ObjectProvider<ProcessBuilder> processBuilderProvider,
            @Autowired ObjectProvider<BlockingBag<Integer, SnowDataFrame>> blockingBagProvider,
            @Autowired @Qualifier("phpsnowConfig.create") ObjectProvider<PhpSnowConfig> configProvider,
            @Autowired @Qualifier("BinaryDecoder") ObjectProvider<StreamDecoder> streamDecoderProvider,
            @Autowired @Qualifier("PlainTextEncoder") ObjectProvider<StreamEncoder> streamEncoderProvider
    ) {
        this.snowStreamProvider = snowStreamProvider;
        this.namedPipesProvider = namedPipesProvider;
        this.phpSnowAppsProvider = phpSnowAppsProvider;
        this.snowDataBufferProvider = snowDataBufferProvider;
        this.streamDecoderProvider = streamDecoderProvider;
        this.streamEncoderProvider = streamEncoderProvider;
        this.blockingBagProvider = blockingBagProvider;
        this.bufferSizeInFrames = bufferSizeInFrames;
        this.configProvider = configProvider;
        this.applicationPid = applicationPid;
        this.namedPipesDir = namedPipesDir;
        this.processBuilderProvider = processBuilderProvider;
    }

    @Bean("snowStream.create")
    @Scope(SCOPE_PROTOTYPE)
    public SnowStream create(String sessionId, Map<String, String> config) {
        PhpSnowConfig phpSnowConfig = configProvider.getObject(config);
        return snowStreamProvider.getObject(
                sessionId,
                phpSnowConfig,
                namedPipesProvider.getObject(sessionId, namedPipesDir),
                phpSnowAppsProvider.getObject(sessionId, phpSnowConfig, applicationPid, processBuilderProvider.getObject()),
                snowDataBufferProvider.getObject(bufferSizeInFrames, blockingBagProvider.getObject()),
                streamDecoderProvider.getObject(),
                streamEncoderProvider.getObject()
        );
    }

}
