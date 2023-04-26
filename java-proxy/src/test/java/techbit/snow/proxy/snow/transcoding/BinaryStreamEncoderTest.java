package techbit.snow.proxy.snow.transcoding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowBackground;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class BinaryStreamEncoderTest {

    private BinaryStreamEncoder encoder;
    private ByteArrayOutputStream out;

    @BeforeEach
    void setup() {
        encoder = new BinaryStreamEncoder();
        out = new ByteArrayOutputStream();
    }


    @Test
    void whenEncodingMetadata_thenBinaryDataAreInOutput() throws IOException {
        SnowAnimationMetadata metadata = new SnowAnimationMetadata(
                99, 101, 15, 73, 987
        );

        encoder.encodeMetadata(metadata, out);

        byte[] expected = new byte[] {
                0, 0, 0, 99,
                0, 0, 0, 101,
                0, 0, 0, 15,
                0, 0, 0, 73,
                0, 0, 3, -37,
        };

        assertArrayEquals(expected, out.toByteArray());
    }

    @Test
    void whenEncodingBackground_thenBinaryDataAreInOutput() throws IOException {
        SnowBackground background = new SnowBackground(4, 4,
            new byte[][] {
                new byte[] { 1, 2, 3, 4 },
                new byte[] { 5, 6, 7, 8 },
                new byte[] { 9, 10, 11, 12 },
                new byte[] { 13, 14, 15, 16 },
            });

        encoder.encodeBackground(background, out);

        byte[] expected = new byte[] {
                0, 0, 0, 4, 0, 0, 0, 4,
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        };

        assertArrayEquals(expected, out.toByteArray());
    }

    @Test
    void whenEncodingFrame_thenBinaryDataAreInOutput() throws IOException {
        SnowDataFrame frame = new SnowDataFrame(
                78, 2,
                new float[] { 103, 22.5f },
                new float[] { 0.5f, 11f },
                new byte[] { 99, 88 }
        );

        encoder.encodeFrame(frame, out);

        byte[] expected = new byte[] {
                0, 0, 0, 78,
                0, 0, 0, 2,

                66, -50, 0, 0,
                63, 0, 0, 0,
                99,

                65, -76, 0, 0,
                65, 48, 0, 0,
                88
        };

        assertArrayEquals(expected, out.toByteArray());
    }


    @Test
    void whenEncodingBasis_thenBinaryDataAreInOutput() throws IOException {
        SnowBasis basis = new SnowBasis(5,
            new int[] { 1, 2, 3, 4, 5 },
            new int[] { 5, 4, 3, 2, 1 },
            new byte[] { 11, 12, 13, 14, 15 }
        );

        encoder.encodeBasis(basis, out);

        byte[] expected = new byte[] {
                0, 0, 0, 5,

                0, 0, 0, 1,
                0, 0, 0, 2,
                0, 0, 0, 3,
                0, 0, 0, 4,
                0, 0, 0, 5,

                0, 0, 0, 5,
                0, 0, 0, 4,
                0, 0, 0, 3,
                0, 0, 0, 2,
                0, 0, 0, 1,

                11, 12, 13, 14, 15
        };

        assertArrayEquals(expected, out.toByteArray());
    }

}