package techbit.snow.proxy.dto;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class SnowAnimationMetadataTest {

    @Test
    void givenValidBinaryData_whenDeserialized_thenCreatesValidMetadata() throws IOException {
        byte[] binary = new byte[]{
                'h', 'e', 'l', 'l', 'o', '-', 'p', 'h', 'p', '-', 's', 'n', 'o', 'w',
                0x0, 0x0, 0x0, 0x7F,
                0x0, 0x1, 0x0, 0x0,
                0x1, 0x0, 0x0, 0x0,
        };

        SnowAnimationMetadata metadata = SnowAnimationMetadata.from(
                new DataInputStream(new ByteArrayInputStream(binary)));

        assertEquals(127, metadata.width());
        assertEquals(65536, metadata.height());
        assertEquals(16777216, metadata.fps());
    }

    @Test
    void givenMetadata_whenSerialized_thenProducesValidBinaryData() throws IOException {
        SnowAnimationMetadata metadata = new SnowAnimationMetadata(127, 65536, 16777216);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        metadata.serialize(new DataOutputStream(output));

        assertArrayEquals(new byte[]{
                0x0, 0x0, 0x0, 0x7F,
                0x0, 0x1, 0x0, 0x0,
                0x1, 0x0, 0x0, 0x0,
        }, output.toByteArray());
    }

    @Test
    void givenDataWithoutMarker_whenDeserialized_thenExceptionIsThrown() throws IOException {
        byte[] binary = new byte[]{
                0x0, 0x0, 0x0, 0x7F,
                0x0, 0x1, 0x0, 0x0,
                0x1, 0x0, 0x0, 0x0,
        };

        assertThrows(Exception.class, () -> SnowAnimationMetadata.from(
                new DataInputStream(new ByteArrayInputStream(binary))));
    }

}