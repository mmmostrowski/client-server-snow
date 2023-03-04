package techbit.snow.proxy.model.serializable;

import java.io.DataInputStream;
import java.io.IOException;

public class SnowDataFrame {
    public final int frameNum;
    public final int chunkSize;
    public final float[] x;
    public final float[] y;
    public final char[] flakeShapes;

    public SnowDataFrame(DataInputStream dataStream) throws IOException {
        frameNum = dataStream.readInt();
        chunkSize = dataStream.readInt();
        x = new float[chunkSize];
        y = new float[chunkSize];
        flakeShapes = new char[chunkSize];

        for (int i = 0; i < chunkSize; ++i) {
            x[i] = dataStream.readFloat();
            y[i] = dataStream.readFloat();
            flakeShapes[i] = dataStream.readChar();
        }
    }
}
