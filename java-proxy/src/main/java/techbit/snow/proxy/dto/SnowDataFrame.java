package techbit.snow.proxy.dto;

import java.util.Arrays;

public record SnowDataFrame(
        int frameNum,
        int chunkSize,
        float[] particlesX,
        float[] particlesY,
        byte[] flakeShapes,
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
        if (basis().equals(basis)) {
            return this;
        }
        return new SnowDataFrame(frameNum, chunkSize, particlesX, particlesY, flakeShapes, basis);
    }

    public float x(int idx) {
        return particlesX[idx];
    }
    public float y(int idx) {
        return particlesY[idx];
    }
    public byte flakeShape(int idx) {
        return flakeShapes[idx];
    }

    @Override
    public String toString() {
        return "SnowDataFrame{\n" +
                "  frameNum=" + frameNum +
                ", chunkSize=" + chunkSize +
                ", particlesX=" + Arrays.toString(particlesX) +
                ", particlesY=" + Arrays.toString(particlesY) +
                ", flakeShapes=" + Arrays.toString(flakeShapes) +
                "\n}";
    }

}
