package techbit.snow.proxy.dto;

public record SnowAnimationMetadata(
        int width,
        int height,
        int fps,
        int bufferSizeInFrames,
        int totalNumberOfFrames)
{

    public static final SnowAnimationMetadata NONE = new SnowAnimationMetadata(
            0, 0, 0, 0, 0);

    @Override
    public String toString() {
        return "SnowAnimationMetadata{" +
                "width=" + width +
                ", height=" + height +
                ", fps=" + fps +
                ", bufferSizeInFrames=" + bufferSizeInFrames +
                ", totalNumberOfFrames=" + totalNumberOfFrames +
                '}';
    }
}
