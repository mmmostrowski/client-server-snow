package techbit.snow.proxy.controller;

import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import techbit.snow.proxy.service.ProxyService;
import techbit.snow.proxy.service.stream.SnowStream;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@Log4j2
public class ProxyController {

        private final ProxyService streaming;

    public ProxyController(
            @Autowired ProxyService streaming
    ) {
        this.streaming = streaming;
    }

    @GetMapping("/")
    public void index() {
        throw new IllegalArgumentException("Invalid url! Missing session id, e.x: http://domain.com/<session-id>");
    }

    @GetMapping("/favicon.ico")
    public void favicon() {
    }

    @GetMapping("/{sessionId}/{*configuration}")
    @Async("streamExecutor")
    public CompletableFuture<StreamingResponseBody> streamToClient(
            final @PathVariable String sessionId,
            final @PathVariable String configuration
    ) {
        log.debug("streamToClient( {}, {} )", sessionId, configuration);

        return CompletableFuture.supplyAsync(() -> out -> {
            try {
                log.debug("streamToClient( {} ) | Async Start", sessionId);

                streaming.stream(sessionId, out, toConfMap(configuration));

                log.debug("streamToClient( {} ) | Async Finished", sessionId);
            } catch (ClientAbortException e) {
                log.debug("streamToClient( {} ) | Client aborted", sessionId);
            } catch (InterruptedException | ConsumerThreadException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @GetMapping({"/stop/{sessionId}", "/stop/{sessionId}/"})
    public Map<String, Object> stopStreaming(final @PathVariable String sessionId) throws IOException, InterruptedException {
        log.debug("stopStreaming( {} )", sessionId);

        streaming.stopStream(sessionId);

        return Map.of(
            "sessionId", sessionId,
            "stopped", "ok"
        );
    }

    @GetMapping({"/details/{sessionId}", "/details/{sessionId}/"})
    public Map<String, Object> streamDetails(final @PathVariable String sessionId) {
        log.debug("streamDetails( {} )", sessionId);

        return Map.of(
            "sessionId", sessionId,
            "exists", streaming.hasStream(sessionId),
            "running", streaming.isRunning(sessionId)
        );
    }

    private static Map<String, String> toConfMap(String configuration) {
        if (Strings.isNullOrEmpty(configuration) || configuration.equals("/")) {
            return Collections.emptyMap();
        }

        Map<String, String> confMap = new HashMap<>();
        String[] elements = configuration.substring(1).split("/");

        if (elements.length % 2 != 0) {
            throw new IllegalArgumentException("Please provide request in form: http://domain.com/sessionId/key1/val1/key2/val2/...");
        }

        String key = null;
        for (int i = 0; i < elements.length; ++i) {
            if (i % 2 == 0) {
                key = elements[i];
                if (Strings.isNullOrEmpty(key)) {
                    throw new IllegalArgumentException("Neither keys nor values can be empty! Please provide request in form: http://domain.com/sessionId/key1/val1/key2/val2/...");
                }
            } else {
                confMap.put(key, elements[i]);
            }
        }
        return confMap;
    }
}
