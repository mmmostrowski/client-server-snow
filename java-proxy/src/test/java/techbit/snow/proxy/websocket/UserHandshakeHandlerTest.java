package techbit.snow.proxy.websocket;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;

import java.security.Principal;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class UserHandshakeHandlerTest {

    @Mock
    private ServerHttpRequest request;
    @Mock
    private WebSocketHandler wsHandler;
    @Mock
    private Map<String, Object> attributes;

    @Test
    void whenDetermineUser_thenUserWithNameIsGenerated() {
        UserHandshakeHandler handler = new UserHandshakeHandler();

        Principal user = handler.determineUser(request, wsHandler, attributes);

        assert user != null;
        Assertions.assertFalse(user.getName().isBlank());
    }

    @Test
    void whenDetermineTwoUsers_thenHavingDifferentNames() {
        UserHandshakeHandler handler = new UserHandshakeHandler();

        Principal user1 = handler.determineUser(request, wsHandler, attributes);
        Principal user2 = handler.determineUser(request, wsHandler, attributes);

        assert user1 != null;
        assert user2 != null;

        Assertions.assertNotEquals(user1.getName(), user2.getName());
    }


}