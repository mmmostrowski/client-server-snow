package techbit.snow.proxy.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.dto.SnowStreamRequestMessage;
import techbit.snow.proxy.service.stream.encoding.BinaryStreamEncoder;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Controller
public class WebsocketsController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebsocketsController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/stream/")
    public void stream(@Payload SnowStreamRequestMessage message) throws InterruptedException, IOException {
        for (int i = 0; i < 100; ++i) {
            StreamEncoder encoder = new BinaryStreamEncoder();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            SnowDataFrame frame = new SnowDataFrame(i,
                    5,
                    new float[]{ 1.0f, 2.0f, 3.0f, 4.0f, 5.0f},
                    new float[]{ 5, 4, 3, 2, 1},
                    new byte[]{ 1, 1, 0, 1, 1}
            );

            encoder.encodeFrame(frame, new DataOutputStream(os));

            messagingTemplate.convertAndSendToUser(message.sessionId(), "/user/stream/", os.toByteArray());

            Thread.sleep(30);
        }
    }

}
