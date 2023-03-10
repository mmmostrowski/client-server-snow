package techbit.snow.proxy.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Value
@RequiredArgsConstructor
public class SnowAnimationMetadata {

    int width;

    int height;

    int fps;

    public SnowAnimationMetadata(DataInputStream inputStream) throws IOException {
        readHelloMarker(inputStream);

        width = inputStream.readInt();
        height = inputStream.readInt();
        fps = inputStream.readInt();
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

}
