package techbit.snow.proxy.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import techbit.snow.proxy.exception.InvalidSessionException;
import techbit.snow.proxy.service.ProxyService;
import techbit.snow.proxy.service.stream.SnowStream;
import techbit.snow.proxy.service.stream.encoding.BinaryStreamEncoder;
import techbit.snow.proxy.service.websocket.SnowStreamTransmitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebsocketsControllerTest {
    @Mock
    private SessionDisconnectEvent sessionDisconnectEvent;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private SnowStreamTransmitter transmitter;
    @Mock
    private ProxyService proxyService;
    private MessageHeaders headers;
    private WebsocketsController controller;

    @BeforeEach
    void setup() {
        controller = spy(new WebsocketsController(messagingTemplate, proxyService));
        headers = new MessageHeaders(Map.of(
                "header-a", "value-a",
                "simpSessionId", "simp-session"
        ));
    }

    @Test
    void givenValidSession_whenStream_thenDelegateToProxyService() throws SnowStream.ConsumerThreadException, IOException, InterruptedException {
        when(proxyService.hasSession("session-id")).thenReturn(true);
        when(controller.createTransmitter(eq("client-id"), any(ByteArrayOutputStream.class))).thenReturn(transmitter);

        controller.stream("client-id", "session-id", headers);

        verify(proxyService).streamSessionTo(
                eq("session-id"),
                any(),
                any(BinaryStreamEncoder.class),
                eq(transmitter)
        );
    }

    @Test
    void givenUnknownSession_whenStream_thenThrowException() {
        Assertions.assertThrows(InvalidSessionException.class, () -> controller.stream("client-id", "session-id", headers));
    }

    @Test
    void givenUnknownSession_whenSessionDisconnectEventOccurs_thenNoErrorOccurs() {
        when(sessionDisconnectEvent.getSessionId()).thenReturn("unknown-session");
        assertDoesNotThrow(() ->controller.onApplicationEvent(sessionDisconnectEvent));
    }

    @Test
    void givenValidSession_whenSessionDisconnectEventOccurs_thenTransmitterIsDeactivated() throws SnowStream.ConsumerThreadException, IOException, InterruptedException {
        when(proxyService.hasSession("session-id")).thenReturn(true);
        when(sessionDisconnectEvent.getSessionId()).thenReturn("simp-session");
        when(controller.createTransmitter(eq("client-id"), any(ByteArrayOutputStream.class))).thenReturn(transmitter);

        controller.stream("client-id", "session-id", headers);
        controller.onApplicationEvent(sessionDisconnectEvent);

        verify(transmitter).deactivate();
    }
}