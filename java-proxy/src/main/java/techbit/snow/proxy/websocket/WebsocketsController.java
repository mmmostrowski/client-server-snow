package techbit.snow.proxy.websocket;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import techbit.snow.proxy.error.InvalidSessionException;
import techbit.snow.proxy.proxy.ProxyService;
import techbit.snow.proxy.snow.stream.SnowStream;
import techbit.snow.proxy.snow.transcoding.BinaryStreamEncoder;
import techbit.snow.proxy.snow.transcoding.StreamEncoder;

import java.io.IOException;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Controller
public class WebsocketsController implements ApplicationListener<SessionDisconnectEvent> {

    private final ProxyService streaming;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, SnowStreamWebsocketTransmitter> transmitters = Maps.newConcurrentMap();

    public WebsocketsController(SimpMessagingTemplate messagingTemplate, ProxyService streaming) {
        this.messagingTemplate = messagingTemplate;
        this.streaming = streaming;
    }

    @MessageMapping("/stream/{sessionId}")
    public void stream(@Payload String clientId, @DestinationVariable String sessionId, MessageHeaders headers)
            throws InterruptedException, IOException, SnowStream.ConsumerThreadException
    {
        if (!streaming.hasSession(sessionId)) {
            throw new InvalidSessionException("Please start session first. Unknown session: " + sessionId);
        }

        final SnowStreamWebsocketTransmitter transmitter = createTransmitter(clientId, new BinaryStreamEncoder());

        transmitters.put(requireNonNull((String) headers.get("simpSessionId")), transmitter);

        streaming.streamSessionTo(sessionId, transmitter);
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(SessionDisconnectEvent event) {
        final String simpSessionId = event.getSessionId();
        if (!transmitters.containsKey(simpSessionId)) {
            return;
        }
        transmitters.get(simpSessionId).deactivate();
        transmitters.remove(simpSessionId);
    }

    SnowStreamWebsocketTransmitter createTransmitter(String clientId, StreamEncoder encoder) {
        return new SnowStreamWebsocketTransmitter(clientId, messagingTemplate, encoder);
    }

}
