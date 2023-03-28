package techbit.snow.proxy.controller;

import lombok.extern.log4j.Log4j2;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import techbit.snow.proxy.service.ProxyService;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;
import techbit.snow.proxy.service.stream.encoding.BinaryStreamEncoder;
import techbit.snow.proxy.service.stream.encoding.PlainTextStreamEncoder;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Log4j2
@RestController
public class ProxyController {

    private final ProxyService streaming;

    public ProxyController(ProxyService streaming) {
        this.streaming = streaming;
    }

    @GetMapping("/")
    public void index() {
        throw new IllegalArgumentException("Invalid url! Missing session id, e.x: http://domain.com/<session-id>");
    }

    @SuppressWarnings("EmptyMethod")
    @GetMapping("/favicon.ico")
    public void favicon() {
    }
//
//    @Async("streamAsyncTaskExecutor")
//    @GetMapping("/{sessionId}/{*configuration}")
//    public CompletableFuture<StreamingResponseBody> streamBinaryToClient(
//            @PathVariable String sessionId,
//            @PathVariable String configuration
//    ) {
//        log.debug("streamBinaryToClient( {}, {} )", sessionId, configuration);
//
//
//        return streamToClient(sessionId, configuration, BinaryStreamEncoder.ENCODER_NAME);
//    }
//
//    @GetMapping({ "/text", "/text/" })
//    public void text() {
//        throw new IllegalArgumentException("Invalid url! Missing session id, e.x: http://domain.com/text/<session-id>");
//    }
//
//    @Async("streamAsyncTaskExecutor")
//    @GetMapping("/text/{sessionId}/{*configuration}")
//    public CompletableFuture<StreamingResponseBody> streamTextToClient(
//            @PathVariable String sessionId,
//            @PathVariable String configuration
//    ) {
//        log.debug("streamTextToClient( {}, {} )", sessionId, configuration);
//
//        return streamToClient(sessionId, configuration, PlainTextStreamEncoder.ENCODER_NAME);
//    }
//
//    private CompletableFuture<StreamingResponseBody> streamToClient(
//            String sessionId, String configuration, String outputType
//    ) {
//        return CompletableFuture.supplyAsync(() -> out -> {
//            try {
//                log.debug("streamToClient( {} ) | Async Start ( {} )", sessionId, outputType);
//
//                streaming.startSession(sessionId, out, outputType, toConfMap(configuration));
//
//                log.debug("streamToClient( {} ) | Async Finished ( {} )", sessionId, outputType);
//            } catch (ClientAbortException e) {
//                log.debug("streamToClient( {} ) | Client aborted ( {} )", sessionId, outputType);
//            } catch (InterruptedException | ConsumerThreadException e) {
//                log.error("streamToClient( {} ) | Error occurred ( {} )", sessionId, outputType);
//                throw new IOException("Streaming interrupted ", e);
//            }
//        });
//    }
//
//    @GetMapping({"/stop/{sessionId}", "/stop/{sessionId}/"})
//    public Map<String, Object> stopStreaming(@PathVariable String sessionId) throws IOException, InterruptedException {
//        log.debug("stopStreaming( {} )", sessionId);
//
//        streaming.stopSession(sessionId);
//
//        return Map.of(
//            "sessionId", sessionId,
//            "stopped", "ok"
//        );
//    }
//
//    @GetMapping({"/details/{sessionId}", "/details/{sessionId}/"})
//    public Map<String, Object> streamDetails(@PathVariable String sessionId) {
//        log.debug("streamDetails( {} )", sessionId);
//
//        return Map.of(
//            "sessionId", sessionId,
//            "exists", streaming.hasSession(sessionId),
//            "running", streaming.isSessionRunning(sessionId)
//        );
//    }
//
//    private Map<String, String> toConfMap(String configuration) {
//        if (configuration.isBlank() || configuration.equals("/")) {
//            return Collections.emptyMap();
//        }
//
//        final String[] elements = configuration.substring(1).split("/");
//        if ((elements.length & 1 ) != 0) {
//            throw new IllegalArgumentException("Please provide request in form: " +
//                    "http://domain.com/sessionId/key1/val1/key2/val2/...");
//        }
//
//        final Map<String, String> confMap = new HashMap<>();
//        for (int i = 0; i < elements.length; i+=2) {
//            final String key = elements[i];
//            final String value = elements[i + 1];
//
//            if (key.isBlank() || value.isBlank()) {
//                throw new IllegalArgumentException("Neither keys nor values can be empty! " +
//                        "Please provide request in form: http://domain.com/sessionId/key1/val1/key2/val2/...");
//            }
//
//            confMap.put(key, value);
//        }
//        return confMap;
//    }
//



}
