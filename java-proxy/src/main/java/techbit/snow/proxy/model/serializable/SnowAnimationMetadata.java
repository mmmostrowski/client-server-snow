package techbit.snow.proxy.model.serializable;

import java.io.DataInputStream;
import java.io.IOException;

public class SnowAnimationMetadata {

    public final int width;

    public final int height;

    public final int fps;

    public SnowAnimationMetadata(DataInputStream inputStream) throws IOException {
        width = inputStream.readInt();
        height = inputStream.readInt();
        fps = inputStream.readInt();
    }

}
