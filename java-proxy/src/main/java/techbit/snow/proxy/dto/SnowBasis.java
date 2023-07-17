package techbit.snow.proxy.dto;

import java.util.Arrays;

import static techbit.snow.proxy.lang.Array.NO_BYTES;
import static techbit.snow.proxy.lang.Array.NO_INTS;

public record SnowBasis(
        int numOfPixels,
        int[] x,
        int[] y,
        byte[] pixels
) {
    public static final SnowBasis NONE = new SnowBasis(0, NO_INTS, NO_INTS, NO_BYTES);

    public int x(int i) {
        return x[i];
    }

    public int y(int i) {
        return y[i];
    }

    public byte pixel(int i) {
        return pixels[i];
    }

    @Override
    public String toString() {
        if (this == NONE) {
            return "Basis.NONE";
        }
        return "Basis{" +
                "numOfPixels=" + numOfPixels +
                ",\n    particlesX=" + Arrays.toString(x) +
                ",\n    particlesY=" + Arrays.toString(y) +
                ",\n    pixels=" + Arrays.toString(pixels) +
                '}';
    }
}
