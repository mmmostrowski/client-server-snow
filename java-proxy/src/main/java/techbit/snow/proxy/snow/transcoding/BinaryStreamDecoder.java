package techbit.snow.proxy.snow.transcoding;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowBackground;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.DataInputStream;
import java.io.IOException;

@Primary
@Component
public final class BinaryStreamDecoder implements StreamDecoder {

    public static final String GREETING_MARKER = "hello-php-snow";

    private int framesCounter;

    @Override
    public SnowAnimationMetadata decodeMetadata(DataInputStream dataStream) throws IOException {
        readHelloMarker(dataStream);

        final int width = dataStream.readInt();
        final int height = dataStream.readInt();
        final int fps = dataStream.readInt();

        return new SnowAnimationMetadata(width, height, fps);
    }

    private void readHelloMarker(DataInputStream inputStream) throws IOException {
        for (char c : GREETING_MARKER.toCharArray()) {
            if (inputStream.readByte() != c) {
                throw new IllegalStateException("Expected greeting in the stream!");
            }
        }
    }

    public SnowBackground decodeBackground(DataInputStream dataStream) throws IOException {
        byte hasBackground = dataStream.readByte();
        if (hasBackground == 0) {
            return SnowBackground.NONE;
        }

        final int canvasWidth = dataStream.readInt();
        final int canvasHeight = dataStream.readInt();
        final byte[][] pixels = new byte[canvasWidth][canvasHeight];
        for (int y = 0; y < canvasHeight; ++y) {
            for (int x = 0; x < canvasWidth; ++x) {
                pixels[x][y] = dataStream.readByte();
            }
        }
        return new SnowBackground(canvasWidth, canvasHeight, pixels);
    }

    @Override
    public SnowDataFrame decodeFrame(DataInputStream dataStream) throws IOException {
        // frame num
        final int frameNum = dataStream.readInt();
        if (frameNum == -1) {
            return SnowDataFrame.LAST;
        }

        if (frameNum != ++framesCounter) {
            throw new IllegalStateException("Binary stream protocol issues! Expected frames in sequence!");
        }

        // particles
        final int chunkSize = dataStream.readInt();
        final float[] particlesX = new float[chunkSize];
        final float[] particlesY = new float[chunkSize];
        final byte[] flakeShapes = new byte[chunkSize];
        for (int i = 0; i < chunkSize; ++i) {
            particlesX[i] = dataStream.readFloat();
            particlesY[i] = dataStream.readFloat();
            flakeShapes[i] = dataStream.readByte();
        }

        return new SnowDataFrame(frameNum, chunkSize, particlesX, particlesY, flakeShapes);
    }

    public SnowBasis decodeBasis(DataInputStream dataStream) throws IOException {
        final int numOfPixels = dataStream.readInt();
        if (numOfPixels == 0) {
            return SnowBasis.NONE;
        }
        final int[] x = new int[numOfPixels];
        final int[] y = new int[numOfPixels];
        final byte[] pixels = new byte[numOfPixels];

        for (int i = 0; i < numOfPixels; ++i) {
            x[i] = dataStream.readInt();
            y[i] = dataStream.readInt();
            pixels[i] = dataStream.readByte();
        }

        return new SnowBasis(numOfPixels, x, y, pixels);
    }

}
