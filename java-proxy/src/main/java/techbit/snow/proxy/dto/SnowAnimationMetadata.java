package techbit.snow.proxy.dto;

import lombok.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record SnowAnimationMetadata (int width, int height, int fps) {

    public static SnowAnimationMetadata from(DataInputStream inputStream) throws IOException {
        readHelloMarker(inputStream);

        int width = inputStream.readInt();
        int height = inputStream.readInt();
        int fps = inputStream.readInt();

        return new SnowAnimationMetadata(width, height, fps);
    }

    private static void readHelloMarker(DataInputStream inputStream) throws IOException {
        for (char c : "hello-php-snow".toCharArray()) {
            if (inputStream.readByte() != c) {
                throw new IllegalStateException("Expected greeting in the stream!");
            }
        }
    }

    public void serialize(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(width);
        outputStream.writeInt(height);
        outputStream.writeInt(fps);
    }

    @Override
    @Generated
    public String toString() {
        return "SnowAnimationMetadata{" +
                "width=" + width +
                ", height=" + height +
                ", fps=" + fps +
                '}';
    }
}
