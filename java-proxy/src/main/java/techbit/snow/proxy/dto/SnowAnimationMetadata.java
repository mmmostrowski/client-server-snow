package techbit.snow.proxy.dto;

public record SnowAnimationMetadata (int width, int height, int fps, int bufferSizeInFrames) {

    @Override
    public String toString() {
        return "SnowAnimationMetadata{" +
                "width=" + width +
                ", height=" + height +
                ", fps=" + fps +
                ", bufferSizeInFrames=" + bufferSizeInFrames +
                '}';
    }
}
