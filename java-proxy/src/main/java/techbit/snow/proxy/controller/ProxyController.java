package techbit.snow.proxy.controller;

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
        return CompletableFuture.supplyAsync(() -> out -> {
            try {
                streaming.stream(sessionId, out);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
