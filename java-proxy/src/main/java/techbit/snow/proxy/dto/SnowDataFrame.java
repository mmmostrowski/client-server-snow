package techbit.snow.proxy.dto;

import lombok.Generated;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

public record SnowDataFrame(
        int frameNum,
        int chunkSize,
        float[] x, float[] y, byte[] flakeShapes,
        Background background,
        Basis basis
) {
    public record Background(int width, int height, byte[][] pixels) {
        @Override
        public String toString() {
            final String pixels = this.pixels == null
                ? "null"
                : Arrays.stream(this.pixels)
                    .map(Arrays::toString)
                    .collect(Collectors.joining("\n    "));

            return "Background{" +
                    "width=" + width +
                    ", height=" + height +
                    ", pixels=\n    " + pixels +
                    "}\n\n";
        }
    }

    public record Basis(int numOfPixels, int[] x, int[] y, byte[] pixels) {
        @Override
        public String toString() {
            return "Basis{" +
                    "numOfPixels=" + numOfPixels +
                    ",\n    x=" + Arrays.toString(x) +
                    ",\n    y=" + Arrays.toString(y) +
                    ",\n    pixels=" + Arrays.toString(pixels) +
                    '}';
        }
    }

    public static final SnowDataFrame LAST = new SnowDataFrame(-1, 0, null, null, null, null, null);
    public static final Background NO_BACKGROUND = new Background(0, 0, null);
    public static final Basis NO_BASIS = new Basis(0, null, null, null);


    public float x(int idx) {
        return x[idx];
    }
    public float y(int idx) {
        return y[idx];
    }
    public byte flakeShape(int idx) {
        return flakeShapes[idx];
    }


    @Override
    public String toString() {
        return "SnowDataFrame{\n" +
                "  frameNum=" + frameNum +
                ", chunkSize=" + chunkSize +
                ", x=" + Arrays.toString(x) +
                ", y=" + Arrays.toString(y) +
                ", flakeShapes=" + Arrays.toString(flakeShapes) +
                "\n  background=" + background +
                "\n  basis=" + basis +
                "\n}";
    }

}
