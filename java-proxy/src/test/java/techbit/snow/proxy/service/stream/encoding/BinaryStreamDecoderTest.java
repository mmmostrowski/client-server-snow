package techbit.snow.proxy.service.stream.encoding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BinaryStreamDecoderTest {

    private BinaryStreamDecoder decoder;

    @BeforeEach
    void setup() {
        decoder = new BinaryStreamDecoder();
    }


    @Test
    void givenValidBinaryData_whenDecodingMetadata_thenCreatesValidEntity() throws IOException {
        byte[] binary = new byte[] {
                'h', 'e', 'l', 'l', 'o', '-', 'p', 'h', 'p', '-', 's', 'n', 'o', 'w',
                0x0, 0x0, 0x0, 0x7F, // width
                0x0, 0x1, 0x0, 0x0,  // height
                0x1, 0x0, 0x0, 0x0,  // fps
        };

        SnowAnimationMetadata metadata = decoder.decodeMetadata(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertEquals(127, metadata.width());
        assertEquals(65536, metadata.height());
        assertEquals(16777216, metadata.fps());
    }

    @Test
    void givenDataWithoutMarker_whenDecodingMetadata_thenExceptionIsThrown() {
        byte[] binary = new byte[] {
                0x0, 0x0, 0x0, 0x7F, // width
                0x0, 0x1, 0x0, 0x0,  // height
                0x1, 0x0, 0x0, 0x0,  // fps
        };

        assertThrows(IllegalStateException.class, () -> decoder.decodeMetadata(
                new DataInputStream(new ByteArrayInputStream(binary))));
    }

    @Test
    void givenValidBinaryData_whenDecodingFrame_thenCreatesValidEntity() throws IOException {
        byte[] binary = new byte[] {
                0x0, 0x0, 0x0, 0x7F,  // frame num
                0x0, 0x0, 0x0, 0x2,   // chunk size
                // chunk 0
                0x0, 0x0, 0x0, 0x0,   // frame1.x
                0x40, 0x20, 0x0, 0x0, // frame1.y
                0x3,                  // frame.flakeShape
                // chunk 1
                0x40, 0x30, 0x0, 0x0, // frame2.x
                0x0, 0x0, 0x0, 0x0,   // frame2.y
                0x4,                  // frame.flakeShape
        };

        SnowDataFrame frame = decoder.decodeFrame(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertEquals(127, frame.frameNum());
        assertEquals(2, frame.chunkSize());

        assertEquals(2, frame.x().length);
        assertEquals(2, frame.y().length);
        assertEquals(2, frame.flakeShapes().length);

        assertEquals(0.0f, frame.x(0));
        assertEquals(2.5f, frame.y(0));
        assertEquals(3, frame.flakeShape(0));
        assertEquals(2.75f, frame.x(1));
        assertEquals(0.0f, frame.y(1));
        assertEquals(4, frame.flakeShape(1));
    }


    @Test
    void givenLastFrame_whenDecodingFrame_thenCreatesValidEntity() throws IOException {
        byte[] binary = new byte[] {
                -1, -1, -1, -1,  // frame num
                0x0, 0x0, 0x0, 0x0,   // chunk size
        };

        SnowDataFrame frame = decoder.decodeFrame(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertEquals(-1, frame.frameNum());
        assertEquals(0, frame.chunkSize());
        assertSame(SnowDataFrame.LAST, frame);
    }

}