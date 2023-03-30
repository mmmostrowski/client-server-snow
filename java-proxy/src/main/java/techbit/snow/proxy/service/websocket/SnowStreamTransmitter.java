package techbit.snow.proxy.service.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.stream.SnowStream;

import java.io.ByteArrayOutputStream;

@Component
public class SnowStreamTransmitter implements SnowStream.Customizations {

    private final String clientId;
    private final SimpMessagingTemplate messagingTemplate;
    private final ByteArrayOutputStream output;

    private boolean isActive = true;

    public SnowStreamTransmitter(String clientId, SimpMessagingTemplate messagingTemplate, ByteArrayOutputStream output) {
        this.clientId = clientId;
        this.messagingTemplate = messagingTemplate;
        this.output = output;
    }

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
        return isActive;
    }

    private void sendToClient() {
        messagingTemplate.convertAndSendToUser(clientId, "/user/stream/", output.toByteArray());
        output.reset();
    }

    public void deactivate() {
        isActive = false;
    }

}
