package techbit.snow.proxy.model.serializable;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class SnowDataFrameTest {

    @Test
    void whenBinaryDataGiven_entityShouldBeCreated() throws IOException {
        byte[] binary = new byte[]{
                0x0, 0x0, 0x0, 0x7F,
                0x0, 0x0, 0x0, 0x2,

                0x0, 0x0, 0x0, 0x0,
                0x40, 0x20, 0x0, 0x0,
                0x3,

                0x40, 0x30, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0,
                0x4,
        };

        SnowDataFrame frame = new SnowDataFrame(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertEquals(127, frame.frameNum);
        assertEquals(2, frame.chunkSize);

        assertEquals(2, frame.x.length);
        assertEquals(2, frame.y.length);
        assertEquals(2, frame.flakeShapes.length);

        assertEquals(0.0f, frame.x[0]);
        assertEquals(2.5f, frame.y[0]);
        assertEquals(3, frame.flakeShapes[0]);
        assertEquals(2.75f, frame.x[1]);
        assertEquals(0.0f, frame.y[1]);
        assertEquals(4, frame.flakeShapes[1]);

    }

//    @Test
//    void whenEntityGiven_itShouldBeSerializable() throws IOException {
//        SnowAnimationMetadata metadata = new SnowAnimationMetadata(127, 65536, 16777216);
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//
//        metadata.serialize(new DataOutputStream(output));
//
//        assertArrayEquals(new byte[]{
//                0x0, 0x0, 0x0, 0x7F,
//                0x0, 0x1, 0x0, 0x0,
//                0x1, 0x0, 0x0, 0x0,
//        }, output.toByteArray());
//    }

}