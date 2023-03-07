package techbit.snow.proxy.model.serializable;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class SnowDataFrame {
    public final int frameNum;
    public final int chunkSize;
    public final float[] x;
    public final float[] y;
    public final byte[] flakeShapes;

    public static SnowDataFrame empty = new SnowDataFrame(0, 0, new float[0], new float[0],new byte[0]);

    public SnowDataFrame(int frameNum, int chunkSize, float[] x, float[] y, byte[] flakeShapes) {
        this.frameNum = frameNum;
        this.chunkSize = chunkSize;
        this.x = x;
        this.y = y;
        this.flakeShapes = flakeShapes;
    }

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

    @Override
    public String toString() {
        return "SnowDataFrame{" +
                "frameNum=" + frameNum +
                ", chunkSize=" + chunkSize +
                ", x=" + Arrays.toString(x) +
                ", y=" + Arrays.toString(y) +
                ", flakeShapes=" + Arrays.toString(flakeShapes) +
                '}';
    }
}
