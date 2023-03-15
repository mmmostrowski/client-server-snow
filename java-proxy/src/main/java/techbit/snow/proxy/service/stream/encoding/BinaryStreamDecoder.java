package techbit.snow.proxy.service.stream.encoding;

import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.DataInputStream;
import java.io.IOException;

@Component
public class BinaryStreamDecoder implements StreamDecoder {

    @Override
    public SnowAnimationMetadata decodeMetadata(DataInputStream dataStream) throws IOException {
        readHelloMarker(dataStream);

        int width = dataStream.readInt();
        int height = dataStream.readInt();
        int fps = dataStream.readInt();

        return new SnowAnimationMetadata(width, height, fps);
    }

    @Override
    public SnowDataFrame decodeFrame(DataInputStream dataStream) throws IOException {
        int frameNum = dataStream.readInt();
        int chunkSize = dataStream.readInt();
        float[] x = new float[chunkSize];
        float[] y = new float[chunkSize];
        byte[] flakeShapes = new byte[chunkSize];

        for (int i = 0; i < chunkSize; ++i) {
            x[i] = dataStream.readFloat();
            y[i] = dataStream.readFloat();
            flakeShapes[i] = dataStream.readByte();
        }
        return new SnowDataFrame(frameNum, chunkSize, x, y, flakeShapes);
    }

    private void readHelloMarker(DataInputStream inputStream) throws IOException {
        for (char c : "hello-php-snow".toCharArray()) {
            if (inputStream.readByte() != c) {
                throw new IllegalStateException("Expected greeting in the stream!");
            }
        }
    }
}
