package techbit.snow.proxy.websocket;

import com.sun.security.auth.UserPrincipal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import techbit.snow.proxy.error.InvalidSessionException;
import techbit.snow.proxy.proxy.ProxyService;
import techbit.snow.proxy.snow.stream.SnowStream;

import java.io.IOException;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebsocketsControllerTest {
    @Mock
    private SessionDisconnectEvent sessionDisconnectEvent;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private SnowStreamWebsocketClient client;
    @Mock
    private ProxyService proxyService;
    private Principal user;
    private WebsocketsController controller;


    @BeforeEach
    void setup() {
        user = new UserPrincipal("UserX");
        controller = spy(new WebsocketsController(messagingTemplate, proxyService));
    }

    @Test
    void givenValidSession_whenStream_thenDelegateToProxyService() throws SnowStream.ConsumerThreadException, IOException, InterruptedException {
        when(proxyService.hasSession("session-id")).thenReturn(true);
        when(controller.createClient("UserX")).thenReturn(client);

        controller.stream("session-id", user);

        verify(proxyService).streamSessionTo(
                eq("session-id"),
                eq(client)
        );
    }

    @Test
    void givenUnknownSession_whenStream_thenThrowException() {
        Assertions.assertThrows(InvalidSessionException.class, () -> controller.stream("session-id", user));
    }

    @Test
    void givenUnknownSession_whenSessionDisconnectEventOccurs_thenNoErrorOccurs() {
        when(sessionDisconnectEvent.getUser()).thenReturn(user);
        assertDoesNotThrow(() -> controller.onApplicationEvent(sessionDisconnectEvent));
    }

    @Test
    void givenValidSession_whenSessionDisconnectEventOccurs_thenTransmitterIsDeactivated() throws SnowStream.ConsumerThreadException, IOException, InterruptedException {
        when(controller.createClient("UserX")).thenReturn(client);
        when(proxyService.hasSession("session-id")).thenReturn(true);
        when(sessionDisconnectEvent.getUser()).thenReturn(user);

        controller.stream("session-id", user);
        controller.onApplicationEvent(sessionDisconnectEvent);

        verify(client).deactivate();
    }
}