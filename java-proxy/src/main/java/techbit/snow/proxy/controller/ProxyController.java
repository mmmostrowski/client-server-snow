package techbit.snow.proxy.controller;

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

    @GetMapping(value = "/{sessionId}")
    @Async("streamExecutor")
    public CompletableFuture<StreamingResponseBody> streamToClient(final @PathVariable String sessionId) {
        logger.debug(() ->  String.format("streamToClient( %s )", sessionId));

        return CompletableFuture.supplyAsync(() -> out -> {
            try {
                logger.debug(() -> String.format("streamToClient( %s ) | Async Start", sessionId));

                streaming.stream(sessionId, out);

                logger.debug(() -> String.format("streamToClient( %s ) | Async Finished", sessionId));
            } catch (ClientAbortException e) {
                logger.debug(() -> String.format("streamToClient( %s ) | Client aborted", sessionId));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @GetMapping(value = "/stop/{sessionId}")
    public String stopStreaming(final @PathVariable String sessionId) {
        logger.debug(() ->  String.format("stopStreaming( %s )", sessionId));

        streaming.stopStream(sessionId);

        return "stopped: " + sessionId + "\n";
    }
}
