package techbit.snow.proxy.service.stream.encoding;

import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class BinaryStreamEncoder implements StreamEncoder {

    @Override
    public void encodeMetadata(SnowAnimationMetadata metadata, OutputStream out) throws IOException {
        DataOutputStream data = new DataOutputStream(out);

        data.writeInt(metadata.width());
        data.writeInt(metadata.height());
        data.writeInt(metadata.fps());
    }

    @Override
    public void encodeFrame(SnowDataFrame frame, OutputStream out) throws IOException {
        DataOutputStream data = new DataOutputStream(out);

        data.writeInt(frame.frameNum());
        data.writeInt(frame.chunkSize());

        for (int i = 0; i < frame.chunkSize(); ++i) {
            data.writeFloat(frame.x(i));
            data.writeFloat(frame.y(i));
            data.writeByte(frame.flakeShape(i));
        }
    }

}
