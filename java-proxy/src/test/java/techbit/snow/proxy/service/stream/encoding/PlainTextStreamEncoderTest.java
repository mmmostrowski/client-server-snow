package techbit.snow.proxy.service.stream.encoding;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.*;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlainTextStreamEncoderTest {

    private PlainTextStreamEncoder encoder;
    private ByteArrayOutputStream out;

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

        assertTrue(outputContainsToken("99"));
        assertTrue(outputContainsToken("101"));
        assertTrue(outputContainsToken("15"));
    }

    @Test
    void whenEncodingFrame_thenHumanReadableDataAreInOutput() throws IOException {
        SnowDataFrame frame = new SnowDataFrame(
                78, 2,
                new float[] { 103, 22.5f },
                new float[] { 0.5f, 11f },
                new byte[] { 99, 88 },
                new SnowDataFrame.Background(3, 2, new byte[][]{
                        new byte[] { 31, 33 },
                        new byte[] { 41, 44 },
                        new byte[] { 51, 55 },
                }),
                new SnowDataFrame.Basis(4,
                        new int[] { 9971, 9972, 9973, 9974 },
                        new int[] { 99711, 99712, 99713, 99714 },
                        new byte[] { 97, 98, 99, 100 }
                )
        );

        encoder.encodeFrame(frame, out);
        
        assertTrue(outputContainsToken("78"));
        assertTrue(outputContainsToken("2"));
        assertTrue(outputContainsToken("103.0"));
        assertTrue(outputContainsToken("22.5"));
        assertTrue(outputContainsToken("0.5"));
        assertTrue(outputContainsToken("11.0"));
        assertTrue(outputContainsToken("99"));
        assertTrue(outputContainsToken("88"));
        assertTrue(outputContainsToken("31"));
        assertTrue(outputContainsToken("33"));
        assertTrue(outputContainsToken("41"));
        assertTrue(outputContainsToken("44"));
        assertTrue(outputContainsToken("51"));
        assertTrue(outputContainsToken("55"));
        assertTrue(outputContainsToken("9971"));
        assertTrue(outputContainsToken("9972"));
        assertTrue(outputContainsToken("9973"));
        assertTrue(outputContainsToken("9974"));
        assertTrue(outputContainsToken("99711"));
        assertTrue(outputContainsToken("99712"));
        assertTrue(outputContainsToken("99713"));
        assertTrue(outputContainsToken("99714"));
        assertTrue(outputContainsToken("97"));
        assertTrue(outputContainsToken("98"));
        assertTrue(outputContainsToken("99"));
        assertTrue(outputContainsToken("100"));
    }

    @Test
    void whenEncodingLastFrame_thenIsAvailableInOutput() throws IOException {
        SnowDataFrame frame = SnowDataFrame.LAST;

        encoder.encodeFrame(frame, out);

        assertTrue(outputContainsToken("-1"));
        assertTrue(outputContainsToken("0"));
    }

    private boolean outputContainsToken(String token) {
        String x = out.toString();
        String[] split = out.toString().split("[^a-zA-Z0-9-.]");
        return Arrays.asList(split).contains(token);
    }

}