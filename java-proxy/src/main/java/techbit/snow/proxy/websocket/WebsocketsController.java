package techbit.snow.proxy.websocket;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import techbit.snow.proxy.error.InvalidSessionException;
import techbit.snow.proxy.proxy.ProxyService;
import techbit.snow.proxy.snow.stream.SnowStream;
import techbit.snow.proxy.snow.transcoding.BinaryStreamEncoder;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
public final class WebsocketsController implements ApplicationListener<SessionDisconnectEvent> {

    private final ProxyService streaming;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Principal, SnowStreamWebsocketClient> clients = Maps.newConcurrentMap();

    public WebsocketsController(SimpMessagingTemplate messagingTemplate, ProxyService streaming) {
        this.messagingTemplate = messagingTemplate;
        this.streaming = streaming;
    }

    @MessageMapping("/stream/{sessionId}")
    public void stream(@DestinationVariable String sessionId, Principal user)
            throws InterruptedException, IOException, SnowStream.ConsumerThreadException
    {
        if (!streaming.hasSession(sessionId)) {
            throw new InvalidSessionException("Please start session first. Unknown session: " + sessionId);
        }

        final SnowStreamWebsocketClient client = createClient(user.getName());
        clients.put(user, client);

        streaming.streamSessionTo(sessionId, client);
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(SessionDisconnectEvent event) {
        final Principal user = event.getUser();
        if (clients.containsKey(user)) {
            clients.get(user).deactivate();
            clients.remove(user);
        }
    }

    SnowStreamWebsocketClient createClient(String clientId) {
        return new SnowStreamWebsocketClient(clientId, messagingTemplate, new BinaryStreamEncoder());
    }

}
