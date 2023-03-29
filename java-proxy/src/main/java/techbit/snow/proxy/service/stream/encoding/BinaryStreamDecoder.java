package techbit.snow.proxy.service.stream.encoding;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.DataInputStream;
import java.io.IOException;

@Primary
@Component
public class BinaryStreamDecoder implements StreamDecoder {

    public static final String GREETING_MARKER = "hello-php-snow";

    @Override
    public SnowAnimationMetadata decodeMetadata(DataInputStream dataStream) throws IOException {
        readHelloMarker(dataStream);

        final int width = dataStream.readInt();
        final int height = dataStream.readInt();
        final int fps = dataStream.readInt();

        return new SnowAnimationMetadata(width, height, fps);
    }

    @Override
    public SnowDataFrame decodeFrame(DataInputStream dataStream) throws IOException {
        final int frameNum = dataStream.readInt();
        final int chunkSize = dataStream.readInt();
        final float[] x = new float[chunkSize];
        final float[] y = new float[chunkSize];
        final byte[] flakeShapes = new byte[chunkSize];

        for (int i = 0; i < chunkSize; ++i) {
            x[i] = dataStream.readFloat();
            y[i] = dataStream.readFloat();
            flakeShapes[i] = dataStream.readByte();
        }
        if (frameNum == -1) {
            return SnowDataFrame.LAST;
        }
        return new SnowDataFrame(frameNum, chunkSize, x, y, flakeShapes);
    }

    private void readHelloMarker(DataInputStream inputStream) throws IOException {
        for (char c : GREETING_MARKER.toCharArray()) {
            if (inputStream.readByte() != c) {
                throw new IllegalStateException("Expected greeting in the stream!");
            }
        }
    }
}
