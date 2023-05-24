package techbit.snow.proxy.snow.transcoding;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.dto.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BinaryStreamDecoderTest {

    private BinaryStreamDecoder decoder;

    @BeforeEach
    void setup() {
        decoder = new BinaryStreamDecoder();
    }


    @Test
    void givenBinaryMetadata_whenDecoding_thenCreatesValidEntity() throws IOException {
        byte[] binary = new byte[]{
                'h', 'e', 'l', 'l', 'o', '-', 'p', 'h', 'p', '-', 's', 'n', 'o', 'w',
                0x0, 0x0, 0x0, 0x7F, // width
                0x0, 0x1, 0x0, 0x0,  // height
                0x1, 0x0, 0x0, 0x0,  // fps
        };

        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(binary));
        SnowAnimationMetadata metadata = decoder.decodeMetadata(
                inputStream,
                new ServerMetadata(Duration.ofSeconds(3)),
                Duration.ofSeconds(7));

        assertEquals(127, metadata.width());
        assertEquals(65536, metadata.height());
        assertEquals(16777216, metadata.fps());
        assertEquals(50331648, metadata.bufferSizeInFrames());
        assertEquals(117440512, metadata.totalNumberOfFrames());
    }

    @Test
    void givenMetadataWithoutMarker_whenDecoding_thenExceptionIsThrown() {
        byte[] binary = new byte[]{
                0x0, 0x0, 0x0, 0x7F, // width
                0x0, 0x1, 0x0, 0x0,  // height
                0x1, 0x0, 0x0, 0x0,  // fps
        };

        assertThrows(IllegalStateException.class, () -> decoder.decodeMetadata(
                new DataInputStream(new ByteArrayInputStream(binary)),
                new ServerMetadata(Duration.ofSeconds(8)),
                Duration.ofMinutes(1)
        ));
    }

    @Test
    void givenEmptyBackground_whenDecoding_thenCreatesValidEntity() throws IOException {
        byte[] binary = new byte[]{
                0x0,
        };

        SnowBackground background = decoder.decodeBackground(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertSame(SnowBackground.NONE, background);
    }

    @Test
    void givenBinaryBackgroundData_whenDecoding_thenCreatesValidEntity() throws IOException {
        byte[] binary = new byte[]{
                0x1,                  // hasBackground
                0x0, 0x0, 0x0, 0x2,   // canvas width
                0x0, 0x0, 0x0, 0x2,   // canvas height
                // pixel matrix
                0x3, 0x5,
                0x7, 0x9,
        };

        SnowBackground background = decoder.decodeBackground(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertEquals(2, background.width());
        assertEquals(2, background.height());
        assertArrayEquals(new byte[][]{
                // y0  y1
                new byte[]{3, 7,}, // x0
                new byte[]{5, 9,}, // x1
        }, background.pixels());
    }

    @Test
    void givenBinaryFrameData_whenDecoding_thenCreatesValidEntity() throws IOException {
        byte[] binary = new byte[]{
                0x0, 0x0, 0x0, 0x1,   // frame num
                0x0, 0x0, 0x0, 0x2,   // chunk size
                // chunk 0
                0x0, 0x0, 0x0, 0x0,   // frame1.particlesX
                0x40, 0x20, 0x0, 0x0, // frame1.particlesY
                0x3,                  // frame.flakeShape
                // chunk 1
                0x40, 0x30, 0x0, 0x0, // frame2.particlesX
                0x0, 0x0, 0x0, 0x0,   // frame2.particlesY
                0x4,                  // frame.flakeShape
        };

        SnowDataFrame frame = decoder.decodeFrame(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertEquals(1, frame.frameNum());
        assertEquals(2, frame.chunkSize());
        assertEquals(2, frame.particlesX().length);
        assertEquals(2, frame.particlesY().length);
        assertEquals(2, frame.flakeShapes().length);
        assertEquals(0.0f, frame.x(0));
        assertEquals(2.5f, frame.y(0));
        assertEquals(3, frame.flakeShape(0));
        assertEquals(2.75f, frame.x(1));
        assertEquals(0.0f, frame.y(1));
        assertEquals(4, frame.flakeShape(1));
        assertSame(SnowBasis.NONE, frame.basis());
    }

    @Test
    void givenFrameNumOutOfSequence_whenDecodingFrame_thenThrowsException() {
        byte[] binary = new byte[]{
                0x0, 0x0, 0x0, 0x11,  // invalid frame num
        };

        Assertions.assertThrows(IllegalStateException.class, () -> decoder.decodeFrame(
                new DataInputStream(new ByteArrayInputStream(binary))));
    }

    @Test
    void givenEmptyBasisData_whenDecoding_thenCreatesValidEntity() throws IOException {
        byte[] binary = new byte[]{
                0x0, 0x0, 0x0, 0x0,  // zero basis pixels
        };

        SnowBasis basis = decoder.decodeBasis(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertSame(SnowBasis.NONE, basis);
    }

    @Test
    void givenBinaryBasisData_whenDecoding_thenCreatesValidEntity() throws IOException {
        byte[] binary = new byte[]{
                0x0, 0x0, 0x0, 0x2,  // two pixels

                0x0, 0x0, 0x0, 0x9,  // pixel 1 X
                0x0, 0x0, 0x0, 0x0,  // pixel 1 Y
                0x7,                 // pixel 1

                0x0, 0x0, 0x3, 0x0,  // pixel 2 X
                0x0, 0x0, 0x0, 0x3,  // pixel 2 Y
                0x9,                 // pixel 2
        };

        SnowBasis basis = decoder.decodeBasis(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertEquals(2, basis.numOfPixels());
        assertEquals(7, basis.pixel(0));
        assertEquals(9, basis.pixel(1));
        assertEquals(9, basis.x(0));
        assertEquals(768, basis.x(1));
        assertEquals(0, basis.y(0));
        assertEquals(3, basis.y(1));
        assertArrayEquals(new int[]{0, 3}, basis.y());
    }

    @Test
    void givenLastFrame_whenDecodingFrame_thenCreatesValidEntity() throws IOException {
        byte[] binary = new byte[]{
                -1, -1, -1, -1,  // last frame num
        };

        SnowDataFrame frame = decoder.decodeFrame(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertSame(SnowDataFrame.LAST, frame);
    }

}