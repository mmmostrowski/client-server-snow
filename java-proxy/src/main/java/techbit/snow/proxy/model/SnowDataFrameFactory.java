package techbit.snow.proxy.model;

public class SnowDataFrameFactory {

    private int frameNumCounter = 0;

    public SnowDataFrame create(int chunkSize, float[] x, float[] y, int[] flakeShapes)
    {
        return new SnowDataFrame(++frameNumCounter, chunkSize, x, y, flakeShapes);
    }

}
