package techbit.snow.proxy.dto;

import java.time.Duration;

public record ServerMetadata(Duration bufferSize) {
    public int bufferSizeInFrames(int fps) {
        return (int) (fps * bufferSize().toMillis() / 1000);
    }
}
