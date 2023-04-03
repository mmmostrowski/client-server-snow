package techbit.snow.proxy.dto;

import java.util.Arrays;
import java.util.Objects;

public record SnowDataFrame(
        int frameNum,
        int chunkSize,
        float[] x, float[] y, byte[] flakeShapes,
        SnowBasis basis
) {

    public static final SnowDataFrame LAST = new SnowDataFrame(
            -1, 0, new float[] {}, new float[] {}, new byte[] {}, SnowBasis.NONE);

    public SnowDataFrame(int frameNum, int chunkSize, float[] x, float[] y, byte[] flakeShapes) {
        this(frameNum, chunkSize, x, y, flakeShapes, SnowBasis.NONE);
    }

    public SnowDataFrame withBasis(SnowBasis basis) {
        if (this == LAST) {
            return LAST;
        }
        if (Objects.equals(basis, basis())) {
            return this;
        }
        return new SnowDataFrame(frameNum, chunkSize, x, y, flakeShapes, basis);
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
    public String toString() {
        return "SnowDataFrame{\n" +
                "  frameNum=" + frameNum +
                ", chunkSize=" + chunkSize +
                ", x=" + Arrays.toString(x) +
                ", y=" + Arrays.toString(y) +
                ", flakeShapes=" + Arrays.toString(flakeShapes) +
                "\n}";
    }

}
