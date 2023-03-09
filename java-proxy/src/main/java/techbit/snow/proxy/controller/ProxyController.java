package techbit.snow.proxy.controller;

import com.google.common.base.Strings;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import techbit.snow.proxy.service.ProxyService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class ProxyController {
    private final Logger logger = LogManager.getLogger(ProxyController.class);

    @Autowired
    private ProxyService streaming;

    @GetMapping(value = "/")
    public void index() {
        throw new IllegalArgumentException("Invalid url! Missing session id, e.x: http://domain.com/<session-id>");
    }

    @GetMapping(value = "/favicon.ico")
    public void favicon() {
    }

    @GetMapping(value = "/{sessionId}/{*configuration}")
    @Async("streamExecutor")
    public CompletableFuture<StreamingResponseBody> streamToClient(
            final @PathVariable String sessionId,
            final @PathVariable String configuration
    ) {
        logger.debug(() ->  String.format("streamToClient( %s, %s )", sessionId, configuration));

        return CompletableFuture.supplyAsync(() -> out -> {
            try {
                logger.debug(() -> String.format("streamToClient( %s ) | Async Start", sessionId));

                streaming.stream(sessionId, out, toConfMap(configuration));

                logger.debug(() -> String.format("streamToClient( %s ) | Async Finished", sessionId));
            } catch (ClientAbortException e) {
                logger.debug(() -> String.format("streamToClient( %s ) | Client aborted", sessionId));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @GetMapping(value = "/stop/{sessionId}")
    public Map<String, Object> stopStreaming(final @PathVariable String sessionId) {
        logger.debug(() ->  String.format("stopStreaming( %s )", sessionId));

        streaming.stopStream(sessionId);

        return Map.of(
            "sessionId", sessionId,
            "stopped", "ok"
        );
    }

    @GetMapping(value = "/details/{sessionId}")
    public Map<String, Object> streamDetails(final @PathVariable String sessionId) {
        logger.debug(() ->  String.format("streamDetails( %s )", sessionId));

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
        String key = null;

        if (elements.length % 2 != 0) {
            throw new IllegalArgumentException("Please provide request in form: http://domain.com/sessionId/key1/val1/key2/val2/...");
        }

        for (int i = 0; i < elements.length; ++i) {
            if (i % 2 == 0) {
                key = elements[i];
            } else {
                confMap.put(key, elements[i]);
            }
        }
        return confMap;
    }
}
