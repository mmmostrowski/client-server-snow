package techbit.snow.proxy.snow.transcoding;

import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowBackground;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Component
public final class BinaryStreamEncoder implements StreamEncoder {

    @Override
    public void encodeMetadata(SnowAnimationMetadata metadata, OutputStream out) throws IOException {
        final DataOutputStream data = new DataOutputStream(out);

        data.writeInt(metadata.width());
        data.writeInt(metadata.height());
        data.writeInt(metadata.fps());
        data.writeInt(metadata.bufferSizeInFrames());
        data.writeInt(metadata.totalNumberOfFrames());
    }

    @Override
    public void encodeBackground(SnowBackground background, OutputStream out) throws IOException {
        final DataOutputStream data = new DataOutputStream(out);

        data.writeInt(background.width());
        if (background.width() <= 0) {
            return;
        }
        data.writeInt(background.height());

        final byte[][] pixels = background.pixels();
        for (int x = 0; x < background.width(); ++x) {
            out.write(pixels[x]);
        }
    }

    @Override
    public void encodeFrame(SnowDataFrame frame, OutputStream out) throws IOException {
        final DataOutputStream data = new DataOutputStream(out);

        data.writeInt(frame.frameNum());
        data.writeInt(frame.chunkSize());

        for (int i = 0; i < frame.chunkSize(); ++i) {
            data.writeFloat(frame.x(i));
            data.writeFloat(frame.y(i));
            data.writeByte(frame.flakeShape(i));
        }
    }

    @Override
    public void encodeBasis(SnowBasis basis, OutputStream out) throws IOException {
        final DataOutputStream data = new DataOutputStream(out);

        data.writeInt(basis.numOfPixels());
        data.write(toBytes(basis.x()));
        data.write(toBytes(basis.y()));
        out.write(basis.pixels());
    }

    private byte[] toBytes(int[] integers) {
        if (integers.length == 0) {
            return new byte[]{};
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(integers.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(integers);
        return byteBuffer.array();
    }

}
