package techbit.snow.proxy.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

import java.io.DataInputStream;
import java.io.IOException;

@Value
@RequiredArgsConstructor
public class SnowDataFrame {

    private static final int LAST_FRAME_NUM = -1;

    public static final SnowDataFrame last = new SnowDataFrame(
            LAST_FRAME_NUM, 0, new float[0], new float[0],new byte[0]);

    int frameNum;
    int chunkSize;
    float[] x;
    float[] y;
    byte[] flakeShapes;

    public SnowDataFrame(DataInputStream dataStream) throws IOException {
        frameNum = dataStream.readInt();
        chunkSize = dataStream.readInt();
        x = new float[chunkSize];
        y = new float[chunkSize];
        flakeShapes = new byte[chunkSize];

        for (int i = 0; i < chunkSize; ++i) {
            x[i] = dataStream.readFloat();
            y[i] = dataStream.readFloat();
            flakeShapes[i] = dataStream.readByte();
        }
    }

    public boolean isLast() {
        return frameNum == LAST_FRAME_NUM;
    }
}
