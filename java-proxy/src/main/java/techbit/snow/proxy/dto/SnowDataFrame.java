package techbit.snow.proxy.dto;

import lombok.Generated;

import java.util.Arrays;

public record SnowDataFrame( int frameNum, int chunkSize, float[] x, float[] y, byte[] flakeShapes ) {

    public static final SnowDataFrame LAST = new SnowDataFrame(
            -1, 0, null, null, null);

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
