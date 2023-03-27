package techbit.snow.proxy.service.stream.encoding;

import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Component(BinaryStreamEncoder.ENCODER_NAME)
public class BinaryStreamEncoder implements StreamEncoder {

    public static final String ENCODER_NAME = "BINARY_ENCODER";


    @Override
    public void encodeMetadata(SnowAnimationMetadata metadata, OutputStream out) throws IOException {
        out.write("metadata".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void encodeFrame(SnowDataFrame frame, OutputStream out) throws IOException {
        out.write(("frame" + frame.frameNum()).getBytes(StandardCharsets.UTF_8));
    }

}
