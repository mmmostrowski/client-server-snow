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

    @BeforeEach
    void setup() {
        encoder = new PlainTextStreamEncoder();
    }

    @Test
    void whenEncodingMetadata_thenHumanReadableDataAreInOutput() throws IOException {
        SnowAnimationMetadata metadata = new SnowAnimationMetadata(99, 101, 15);

        OutputStream out = new ByteArrayOutputStream();
        encoder.encodeMetadata(metadata, out);
        String result = out.toString();

        assertTrue(result.contains("99"));
        assertTrue(result.contains("101"));
        assertTrue(result.contains("15"));
    }

    @Test
    void whenEncodingFrame_thenHumanReadableDataAreInOutput() throws IOException {
        SnowDataFrame frame = new SnowDataFrame(78, 2,
                new float[]{103, 22.5f},
                new float[]{0.5f, 11f},
                new byte[]{0, 8});

        OutputStream out = new ByteArrayOutputStream();
        encoder.encodeFrame(frame, out);
        String result = out.toString();

        assertTrue(result.contains("78"));
        assertTrue(result.contains("2"));
        assertTrue(result.contains("103"));
        assertTrue(result.contains("22.5"));
        assertTrue(result.contains("0.5"));
        assertTrue(result.contains("1"));
        assertTrue(result.contains("0"));
        assertTrue(result.contains("8"));
    }
}