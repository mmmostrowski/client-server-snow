package techbit.snow.proxy.service.websocket;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import techbit.snow.proxy.dto.SnowAnimationBackground;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.ByteArrayOutputStream;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnowStreamTransmitterTest {
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ByteArrayOutputStream output;
    @Mock
    private SnowAnimationMetadata metadata;
    @Mock
    private SnowAnimationBackground background;
    @Mock
    private SnowDataFrame snowDataFrame;
    private byte[] byteArray;
    private SnowStreamTransmitter transmitter;

    @BeforeEach
    void setup() {
        byteArray = new byte[] { 1, 2, 3 };

        transmitter = new SnowStreamTransmitter("client-id", messagingTemplate, output);
    }

    @Test
    void whenBrandNewTransmitter_thenHasActiveStream() {
        Assertions.assertTrue(transmitter.isAnimationActive());
    }

    @Test
    void whenTransmitterDeactivated_thenHasInactiveStream() {
        transmitter.deactivate();
        Assertions.assertFalse(transmitter.isAnimationActive());
    }

    @Test
    void whenMetadataEncodedIntoOutputStream_thenFlushItToWebsocketMessage() {
        when(output.toByteArray()).thenReturn(byteArray);

        transmitter.onAnimationInitialized(metadata, background);

        InOrder inOrder = inOrder(messagingTemplate, output);
        inOrder.verify(messagingTemplate).convertAndSendToUser(
                "client-id", "/user/stream/", byteArray);
        inOrder.verify(output).reset();
    }

    @Test
    void whenSnowDataFrameEncodedIntoOutputStream_thenFlushItToWebsocketMessage() {
        when(output.toByteArray()).thenReturn(byteArray);

        transmitter.onFrameSent(snowDataFrame);

        InOrder inOrder = inOrder(messagingTemplate, output);
        inOrder.verify(messagingTemplate).convertAndSendToUser(
                "client-id", "/user/stream/", byteArray);
        inOrder.verify(output).reset();
    }

}