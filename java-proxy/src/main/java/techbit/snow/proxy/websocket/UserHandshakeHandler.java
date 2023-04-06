package techbit.snow.proxy.websocket;

import com.sun.security.auth.UserPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.AbstractHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("NullableProblems")
class UserHandshakeHandler extends AbstractHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String uniqueId = UUID.randomUUID().toString();
        return new UserPrincipal(uniqueId);
    }
}
