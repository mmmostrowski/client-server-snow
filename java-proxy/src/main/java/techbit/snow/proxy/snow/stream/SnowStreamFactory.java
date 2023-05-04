package techbit.snow.proxy.snow.stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import techbit.snow.proxy.config.PhpSnowConfig;
import techbit.snow.proxy.config.PhpSnowConfigConverter;
import techbit.snow.proxy.dto.ServerMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.snow.php.NamedPipe;
import techbit.snow.proxy.snow.php.PhpSnowApp;
import techbit.snow.proxy.snow.transcoding.BinaryStreamDecoder;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Primary
@Service
@Scope(SCOPE_PROTOTYPE)
public final class SnowStreamFactory {

    private final Path pipesDir;
    private final String applicationPid;
    private final Duration bufferSize;
    private final String bootstrapLocation;
    private final PhpSnowConfigConverter configProvider;
    private final ApplicationEventPublisher applicationEventPublisher;


    public SnowStreamFactory(
            @Value("${phpsnow.buffer-size-in-milliseconds}") int bufferSize,
            @Value("${phpsnow.bootstrap}") String bootstrapLocation,
            ApplicationEventPublisher applicationEventPublisher,
            PhpSnowConfigConverter configProvider,
            String applicationPid,
            Path pipesDir
    ) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.bufferSize = Duration.ofMillis(bufferSize);
        this.bootstrapLocation = bootstrapLocation;
        this.applicationPid = applicationPid;
        this.configProvider = configProvider;
        this.pipesDir = pipesDir;
    }

    public SnowStream create(String sessionId, Map<String, String> config) {
        final PhpSnowConfig phpSnowConfig = configProvider.fromMap(config);
        final ServerMetadata serverMetadata = createServerMetadata();
        return createSnowStream(sessionId,
                phpSnowConfig,
                createPipe(sessionId, pipesDir),
                createPhpSnowApp(sessionId, phpSnowConfig, applicationPid, new ProcessBuilder()),
                createSnowDataBuffer(serverMetadata.bufferSizeInFrames(phpSnowConfig.fps()), new BlockingBag<>()),
                createBinaryStreamDecoder(),
                serverMetadata,
                applicationEventPublisher);
    }

    SnowStream createSnowStream(
            String sessionId, PhpSnowConfig phpSnowConfig, NamedPipe pipe,
            PhpSnowApp phpSnowApp, SnowDataBuffer snowDataBuffer,
            BinaryStreamDecoder binaryStreamDecoder,
            ServerMetadata serverMetadata,
            ApplicationEventPublisher applicationEventPublisher) {
        return new SnowStream(sessionId,
                phpSnowConfig,
                serverMetadata,
                pipe,
                phpSnowApp,
                snowDataBuffer,
                binaryStreamDecoder,
                applicationEventPublisher
        );
    }

    PhpSnowApp createPhpSnowApp(String sessionId, PhpSnowConfig phpSnowConfig, String applicationPid, ProcessBuilder processBuilder) {
        return new PhpSnowApp(sessionId, phpSnowConfig, applicationPid, processBuilder, bootstrapLocation);
    }

    SnowDataBuffer createSnowDataBuffer(int maxNumOfFrames, BlockingBag<Integer, SnowDataFrame> frames) {
        return new SnowDataBuffer(maxNumOfFrames, frames);
    }

    NamedPipe createPipe(String sessionId, Path pipesDir) {
        return new NamedPipe(sessionId, pipesDir);
    }

    BinaryStreamDecoder createBinaryStreamDecoder() {
        return new BinaryStreamDecoder();
    }

    ServerMetadata createServerMetadata() {
        return new ServerMetadata(bufferSize);
    }

}
