package techbit.snow.proxy.service.stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfigFactory;
import techbit.snow.proxy.service.stream.encoding.BinaryStreamDecoder;
import techbit.snow.proxy.service.stream.encoding.PlainTextStreamEncoder;

import java.nio.file.Path;
import java.util.Map;

@Component
@Primary
public class SnowStreamFactoryImpl implements SnowStreamFactory {

    private final int bufferSizeInFrames;

    private final PhpSnowConfigFactory configProvider;

    private final String applicationPid;

    private final Path pipesDir;


    public SnowStreamFactoryImpl(
            @Autowired Path pipesDir,
            @Value("${phpsnow.buffer-size-in-frames}") int bufferSizeInFrames,
            @Autowired String applicationPid,
            @Autowired PhpSnowConfigFactory configProvider
    ) {
        this.bufferSizeInFrames = bufferSizeInFrames;
        this.configProvider = configProvider;
        this.applicationPid = applicationPid;
        this.pipesDir = pipesDir;
    }

    @Override
    public SnowStream create(String sessionId, Map<String, String> config) {
        final PhpSnowConfig phpSnowConfig = configProvider.create(config);
        return createSnowStream(sessionId,
                phpSnowConfig,
                createPipe(sessionId, pipesDir),
                createPhpSnowApp(sessionId, phpSnowConfig, applicationPid, new ProcessBuilder()),
                createSnowDataBuffer(bufferSizeInFrames, new BlockingBag<>()),
                createBinaryStreamDecoder(),
                createPlainTextStreamEncoder());
    }

    SnowStream createSnowStream(
            String sessionId, PhpSnowConfig phpSnowConfig, NamedPipe pipe,
            PhpSnowApp phpSnowApp, SnowDataBuffer snowDataBuffer,
            BinaryStreamDecoder binaryStreamDecoder,
            PlainTextStreamEncoder plainTextStreamEncoder
    ) {
        return new SnowStream(sessionId,
                phpSnowConfig,
                pipe,
                phpSnowApp,
                snowDataBuffer,
                binaryStreamDecoder,
                plainTextStreamEncoder
        );
    }

    PlainTextStreamEncoder createPlainTextStreamEncoder() {
        return new PlainTextStreamEncoder();
    }

    BinaryStreamDecoder createBinaryStreamDecoder() {
        return new BinaryStreamDecoder();
    }

    SnowDataBuffer createSnowDataBuffer(int maxNumOfFrames, BlockingBag<Integer, SnowDataFrame> frames) {
        return new SnowDataBuffer(maxNumOfFrames, frames);
    }

    PhpSnowApp createPhpSnowApp(String sessionId, PhpSnowConfig phpSnowConfig, String applicationPid, ProcessBuilder processBuilder) {
        return new PhpSnowApp(sessionId, phpSnowConfig, applicationPid, processBuilder);
    }

    NamedPipe createPipe(String sessionId, Path pipesDir) {
        return new NamedPipe(sessionId, pipesDir);
    }

}
