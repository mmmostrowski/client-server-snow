package techbit.snow.proxy.controller;

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
import techbit.snow.proxy.exception.InvalidSessionException;
import techbit.snow.proxy.service.ProxyService;
import techbit.snow.proxy.service.stream.SnowStream;
import techbit.snow.proxy.service.stream.encoding.BinaryStreamEncoder;
import techbit.snow.proxy.service.websocket.SnowStreamTransmitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Controller
public class WebsocketsController implements ApplicationListener<SessionDisconnectEvent> {

    private final ProxyService streaming;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, SnowStreamTransmitter> transmitters = Maps.newConcurrentMap();

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

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final SnowStreamTransmitter transmitter = createTransmitter(clientId, output);
        transmitters.put((String)headers.get("simpSessionId"), transmitter);

        streaming.streamSessionTo(sessionId, output, new BinaryStreamEncoder(), transmitter);
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

    SnowStreamTransmitter createTransmitter(String clientId, ByteArrayOutputStream output) {
        return new SnowStreamTransmitter(clientId, messagingTemplate, output);
    }

}
