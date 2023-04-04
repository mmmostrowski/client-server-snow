package techbit.snow.proxy.websocket;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowBackground;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.snow.transcoding.StreamEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnowStreamWebsocketClientTest {
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ByteArrayOutputStream output;
    @Mock
    private SnowAnimationMetadata metadata;
    @Mock
    private SnowBackground background;
    @Mock
    private SnowDataFrame snowDataFrame;
    @Mock
    private StreamEncoder encoder;
    private byte[] byteArray;
    private SnowStreamWebsocketClient transmitter;

    @BeforeEach
    void setup() {
        byteArray = new byte[] { 1, 2, 3 };

        transmitter = new SnowStreamWebsocketClient("client-id", messagingTemplate, encoder, output);
    }

    @Test
    void whenBrandNewTransmitter_thenHasActiveStream() {
        Assertions.assertTrue(transmitter.continueStreaming());
    }

    @Test
    void whenTransmitterDeactivated_thenHasInactiveStream() {
        transmitter.deactivate();
        Assertions.assertFalse(transmitter.continueStreaming());
    }

    @Test
    void whenStreamingEnded_thenHasInactiveStream() throws IOException {
        transmitter.stopStreaming();
        Assertions.assertFalse(transmitter.continueStreaming());
    }

    @Test
    void whenMetadataEncodedIntoOutputStream_thenFlushItToWebsocketMessage() throws IOException {
        when(output.toByteArray()).thenReturn(byteArray);

        transmitter.startStreaming(metadata, background);

        InOrder inOrder = inOrder(messagingTemplate, output);
        inOrder.verify(messagingTemplate).convertAndSendToUser(
                "client-id", "/user/stream/", byteArray);
        inOrder.verify(output).reset();
    }

    @Test
    void whenSnowDataFrameEncodedIntoOutputStream_thenFlushItToWebsocketMessage() throws IOException {
        when(output.toByteArray()).thenReturn(byteArray);

        transmitter.streamFrame(snowDataFrame, SnowBasis.NONE);

        InOrder inOrder = inOrder(messagingTemplate, output);
        inOrder.verify(messagingTemplate).convertAndSendToUser(
                "client-id", "/user/stream/", byteArray);
        inOrder.verify(output).reset();
    }

}