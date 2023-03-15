package techbit.snow.proxy.dto;

import lombok.Generated;

public record SnowAnimationMetadata (int width, int height, int fps) {

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
