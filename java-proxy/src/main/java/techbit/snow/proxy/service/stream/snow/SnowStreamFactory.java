package techbit.snow.proxy.service.stream.snow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfigConverter;
import techbit.snow.proxy.service.stream.BlockingBag;
import techbit.snow.proxy.service.stream.NamedPipe;
import techbit.snow.proxy.service.stream.encoding.BinaryStreamDecoder;
import techbit.snow.proxy.service.stream.snow.SnowDataBuffer;
import techbit.snow.proxy.service.stream.snow.SnowStream;

import java.nio.file.Path;
import java.util.Map;

@Primary
@Component
public class SnowStreamFactory {

    private final Path pipesDir;
    private final String applicationPid;
    private final int bufferSizeInFrames;
    private final String bootstrapLocation;
    private final PhpSnowConfigConverter configProvider;
    private final ApplicationEventPublisher applicationEventPublisher;


    public SnowStreamFactory(
            @Value("${phpsnow.buffer-size-in-frames}") int bufferSizeInFrames,
            @Value("${phpsnow.bootstrap}") String bootstrapLocation,
            ApplicationEventPublisher applicationEventPublisher,
            PhpSnowConfigConverter configProvider,
            String applicationPid,
            Path pipesDir
    ) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.bufferSizeInFrames = bufferSizeInFrames;
        this.bootstrapLocation = bootstrapLocation;
        this.applicationPid = applicationPid;
        this.configProvider = configProvider;
        this.pipesDir = pipesDir;
    }

    public SnowStream create(String sessionId, Map<String, String> config) {
        final PhpSnowConfig phpSnowConfig = configProvider.fromMap(config);
        return createSnowStream(sessionId,
                phpSnowConfig,
                createPipe(sessionId, pipesDir),
                createPhpSnowApp(sessionId, phpSnowConfig, applicationPid, new ProcessBuilder()),
                createSnowDataBuffer(bufferSizeInFrames, new BlockingBag<>()),
                createBinaryStreamDecoder(),
                applicationEventPublisher);
    }

    SnowStream createSnowStream(
            String sessionId, PhpSnowConfig phpSnowConfig, NamedPipe pipe,
            PhpSnowApp phpSnowApp, SnowDataBuffer snowDataBuffer,
            BinaryStreamDecoder binaryStreamDecoder,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        return new SnowStream(sessionId,
                phpSnowConfig,
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

}
