package techbit.snow.proxy.controller;

import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.ProxyService;
import techbit.snow.proxy.service.stream.SnowStream;
import techbit.snow.proxy.service.stream.encoding.BinaryStreamEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

@Controller
public class WebsocketsController implements ApplicationListener<SessionDisconnectEvent> {

    private final SimpMessagingTemplate messagingTemplate;

    private final ProxyService streaming;

    private final Set<String> activeClients = Sets.newConcurrentHashSet();

    public WebsocketsController(SimpMessagingTemplate messagingTemplate, ProxyService streaming) {
        this.messagingTemplate = messagingTemplate;
        this.streaming = streaming;
    }

    @MessageMapping("/stream/{sessionId}")
    public void stream(@Payload String clientId, @DestinationVariable String sessionId, MessageHeaders headers)
            throws InterruptedException, IOException, SnowStream.ConsumerThreadException
    {
        if (!streaming.hasSession(sessionId)) {
            throw new IllegalArgumentException("Please start session first. Unknown session: " + sessionId);
        }

        final String simpSessionId = (String) headers.get("simpSessionId");
        activeClients.add(simpSessionId);

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        streaming.streamSessionTo(sessionId, os, new BinaryStreamEncoder(), new SnowStream.Customizations() {
            @Override
            public void onMetadataEncoded(SnowAnimationMetadata metadata) {
                sendToClient();
            }

            @Override
            public void onFrameEncoded(SnowDataFrame frame) {
                sendToClient();
            }

            @Override
            public boolean isStreamActive() {
                return activeClients.contains(simpSessionId);
            }

            private void sendToClient() {
                messagingTemplate.convertAndSendToUser(clientId, "/user/stream/", os.toByteArray());
                os.reset();
            }
        });
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(SessionDisconnectEvent event) {
        activeClients.remove(event.getSessionId());
    }

}
