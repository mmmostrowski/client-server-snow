package techbit.snow.proxy.dto;

public record SnowAnimationMetadata (int width, int height, int fps) {

    @Override
    public String toString() {
        return "SnowAnimationMetadata{" +
                "width=" + width +
                ", height=" + height +
                ", fps=" + fps +
                '}';
    }

}
