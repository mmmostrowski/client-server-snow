package techbit.snow.proxy.dto;

import lombok.Generated;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public record SnowDataFrame( int frameNum, int chunkSize, float[] x, float[] y, byte[] flakeShapes ) {

    public static final SnowDataFrame empty = new SnowDataFrame(
        0, 0, new float[0], new float[0],new byte[0]);
    public static final SnowDataFrame last = new SnowDataFrame(
            -1, 0, new float[0], new float[0],new byte[0]);

    public boolean isValidDataFrame() {
        return frameNum > 0;
    }

    public float x(int idx) {
        return x[idx];
    }

    public float y(int idx) {
        return y[idx];
    }

    public byte flakeShape(int idx) {
        return flakeShapes[idx];
    }

    @Override
    @Generated
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
