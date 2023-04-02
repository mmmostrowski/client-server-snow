package techbit.snow.proxy.service.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import techbit.snow.proxy.dto.SnowAnimationBackground;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.stream.SnowStream;

import java.io.ByteArrayOutputStream;

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
    public void onAnimationInitialized(SnowAnimationMetadata metadata, SnowAnimationBackground background) {
        sendToClient();
    }

    @Override
    public void onFrameSent(SnowDataFrame frame) {
        sendToClient();
    }

    @Override
    public boolean isAnimationActive() {
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
