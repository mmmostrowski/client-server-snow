package techbit.snow.proxy.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowBackground;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.snow.stream.SnowStreamClient;
import techbit.snow.proxy.snow.transcoding.StreamEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class SnowStreamWebsocketClient implements SnowStreamClient {

    private final String clientId;
    private final StreamEncoder encoder;
    private final ByteArrayOutputStream output;
    private final SimpMessagingTemplate messagingTemplate;
    private boolean isActive = true;

    public SnowStreamWebsocketClient(String clientId, SimpMessagingTemplate messagingTemplate, StreamEncoder encoder) {
        this(clientId, messagingTemplate, encoder, new ByteArrayOutputStream());
    }

    SnowStreamWebsocketClient(String clientId, SimpMessagingTemplate messagingTemplate, StreamEncoder encoder,
                              ByteArrayOutputStream output)
    {
        this.clientId = clientId;
        this.messagingTemplate = messagingTemplate;
        this.output = output;
        this.encoder = encoder;
    }

    @Override
    public void startStreaming(SnowAnimationMetadata metadata, SnowBackground background) throws IOException {
        encoder.encodeMetadata(metadata, output);
        encoder.encodeBackground(background, output);
        sendToWebsocketClient();
    }

    @Override
    public void streamFrame(SnowDataFrame frame, SnowBasis basis) throws IOException {
        encoder.encodeFrame(frame, output);
        encoder.encodeBasis(basis, output);
        sendToWebsocketClient();
    }

    @Override
    public void stopStreaming() throws IOException {
        encoder.encodeFrame(SnowDataFrame.LAST, output);
        isActive = false;
    }

    @Override
    public boolean continueStreaming() {
        return isActive;
    }

    private void sendToWebsocketClient() {
        messagingTemplate.convertAndSendToUser(clientId, "/stream/", output.toByteArray());
        output.reset();
    }

    public void deactivate() {
        isActive = false;
    }

}
