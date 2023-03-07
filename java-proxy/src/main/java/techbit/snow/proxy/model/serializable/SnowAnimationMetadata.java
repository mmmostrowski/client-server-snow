package techbit.snow.proxy.model.serializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class SnowAnimationMetadata {

    public final int width;

    public final int height;

    public final int fps;

    public SnowAnimationMetadata(int width, int height, int fps) {
        this.width = width;
        this.height = height;
        this.fps = fps;
    }

    public SnowAnimationMetadata(DataInputStream inputStream) throws IOException {
        width = inputStream.readInt();
        height = inputStream.readInt();
        fps = inputStream.readInt();
    }

    public void serialize(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(width);
        outputStream.writeInt(height);
        outputStream.writeInt(fps);
    }

    @Override
    public String toString() {
        return "SnowAnimationMetadata{" +
                "width=" + width +
                ", height=" + height +
                ", fps=" + fps +
                '}';
    }
}
