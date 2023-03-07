package techbit.snow.proxy.model.serializable;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class SnowAnimationMetadataTest {

    @Test
    void whenBinaryDataGiven_entityShouldBeCreated() throws IOException {
        byte[] binary = new byte[]{
                0x0, 0x0, 0x0, 0x7F,
                0x0, 0x1, 0x0, 0x0,
                0x1, 0x0, 0x0, 0x0,
        };

        SnowAnimationMetadata metadata = new SnowAnimationMetadata(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertEquals(127, metadata.width);
        assertEquals(65536, metadata.height);
        assertEquals(16777216, metadata.fps);
    }

    @Test
    void whenEntityGiven_itShouldBeSerializable() throws IOException {
        SnowAnimationMetadata metadata = new SnowAnimationMetadata(127, 65536, 16777216);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        metadata.serialize(new DataOutputStream(output));

        assertArrayEquals(new byte[]{
                0x0, 0x0, 0x0, 0x7F,
                0x0, 0x1, 0x0, 0x0,
                0x1, 0x0, 0x0, 0x0,
        }, output.toByteArray());
    }

}