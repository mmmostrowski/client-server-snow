package techbit.snow.proxy.service.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowBackground;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;
import techbit.snow.proxy.service.stream.snow.SnowStreamSimpleClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SnowStreamWebsocketTransmitter extends SnowStreamSimpleClient {

    private final String clientId;
    private final ByteArrayOutputStream output;
    private boolean isActive = true;
    private final SimpMessagingTemplate messagingTemplate;

    public SnowStreamWebsocketTransmitter(String clientId, SimpMessagingTemplate messagingTemplate, StreamEncoder encoder) {
        this(clientId, messagingTemplate, encoder, new ByteArrayOutputStream());
    }

    SnowStreamWebsocketTransmitter(String clientId, SimpMessagingTemplate messagingTemplate, StreamEncoder encoder,
                                           ByteArrayOutputStream output)
    {
        super(encoder, output);
        this.clientId = clientId;
        this.messagingTemplate = messagingTemplate;
        this.output = output;
    }

    @Override
    public void startStreaming(SnowAnimationMetadata metadata, SnowBackground background) throws IOException {
        super.startStreaming(metadata, background);
        sendToWebsocketClient();
    }

    @Override
    public void streamFrame(SnowDataFrame frame, SnowBasis basis) throws IOException {
        super.streamFrame(frame, basis);
        sendToWebsocketClient();
    }

    @Override
    public void stopStreaming() throws IOException {
        super.stopStreaming();
        isActive = false;
    }

    @Override
    public boolean continueStreaming() {
        return isActive;
    }

    private void sendToWebsocketClient() {
        messagingTemplate.convertAndSendToUser(clientId, "/user/stream/", output.toByteArray());
        output.reset();
    }

    public void deactivate() {
        isActive = false;
    }

}
