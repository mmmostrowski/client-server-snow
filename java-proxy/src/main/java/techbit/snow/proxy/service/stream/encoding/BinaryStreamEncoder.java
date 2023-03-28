package techbit.snow.proxy.service.stream.encoding;

import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.DataOutputStream;
import java.io.IOException;

@Component(BinaryStreamEncoder.ENCODER_NAME)
public class BinaryStreamEncoder implements StreamEncoder {

    public static final String ENCODER_NAME = "BINARY_ENCODER";


    @Override
    public void encodeMetadata(SnowAnimationMetadata metadata, DataOutputStream out) throws IOException {
        out.writeInt(metadata.width());
        out.writeInt(metadata.height());
        out.writeInt(metadata.fps());
    }

    @Override
    public void encodeFrame(SnowDataFrame frame, DataOutputStream out) throws IOException {
        out.writeInt(frame.frameNum());
        out.writeInt(frame.chunkSize());

        for (int i = 0; i < frame.chunkSize(); ++i) {
            out.writeFloat(frame.x(i));
            out.writeFloat(frame.y(i));
            out.writeByte(frame.flakeShape(i));
        }
    }

}
