package techbit.snow.proxy.model;

public record SnowDataFrame(int frameNum, int chunkSize, float[] x, float[] y, int[] flakeShapes) {
}
