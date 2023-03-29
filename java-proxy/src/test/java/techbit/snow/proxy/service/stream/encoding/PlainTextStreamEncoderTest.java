package techbit.snow.proxy.service.stream.encoding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlainTextStreamEncoderTest {

    private PlainTextStreamEncoder encoder;
    private OutputStream out;

    @BeforeEach
    void setup() {
        encoder = new PlainTextStreamEncoder();
        out = new ByteArrayOutputStream();
    }


    @Test
    void whenEncodingMetadata_thenHumanReadableDataAreInOutput() throws IOException {
        SnowAnimationMetadata metadata = new SnowAnimationMetadata(
                99, 101, 15
        );

        encoder.encodeMetadata(metadata, out);

        assertTrue(out.toString().contains("99"));
        assertTrue(out.toString().contains("101"));
        assertTrue(out.toString().contains("15"));
    }

    @Test
    void whenEncodingFrame_thenHumanReadableDataAreInOutput() throws IOException {
        SnowDataFrame frame = new SnowDataFrame(
                78, 2,
                new float[] { 103, 22.5f },
                new float[] { 0.5f, 11f },
                new byte[] { 99, 88 }
        );

        encoder.encodeFrame(frame, out);

        assertTrue(out.toString().contains("78"));
        assertTrue(out.toString().contains("2"));
        assertTrue(out.toString().contains("103"));
        assertTrue(out.toString().contains("22.5"));
        assertTrue(out.toString().contains("0.5"));
        assertTrue(out.toString().contains("11"));
        assertTrue(out.toString().contains("99"));
        assertTrue(out.toString().contains("88"));
    }

    @Test
    void whenEncodingLastFrame_thenIsAvailableInOutput() throws IOException {
        SnowDataFrame frame = SnowDataFrame.LAST;

        encoder.encodeFrame(frame, out);

        assertTrue(out.toString().contains("-1"));
        assertTrue(out.toString().contains("0"));
    }

}