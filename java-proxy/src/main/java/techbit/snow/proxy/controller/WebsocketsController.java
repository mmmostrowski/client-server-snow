package techbit.snow.proxy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.BinaryMessage;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.stream.encoding.BinaryStreamEncoder;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

@Controller
public class WebsocketsController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebsocketsController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/news")
    @SendTo("/topic/news2")
    public void broadcastNews(@Payload String message) throws InterruptedException, IOException {
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

            messagingTemplate.convertAndSend("/topic/news2", os.toByteArray());

            Thread.sleep(30);
        };
    }

//    @MessageMapping("/mywebsockets")
//    @SendToUser("/queue/greetings")
//    public String reply(@Payload String message,
//                        Principal user) {
//        return  "Hello " + message;
//    }
//
}
