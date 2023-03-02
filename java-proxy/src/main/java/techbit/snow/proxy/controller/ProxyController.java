package techbit.snow.proxy.controller;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import techbit.snow.proxy.service.ProxyService;

@RestController
public class ProxyController {

    @Autowired
    private ProxyService streaming;

    @GetMapping(value = "/")
    public void index() {
        throw new IllegalArgumentException("Invalid url! Missing session id, e.x: http://domain.com/<session-id>");
    }

    @GetMapping(value = "/{sessionId}")
    public StreamingResponseBody streamToClient(final @PathVariable String sessionId) {
        return out -> streaming.stream(sessionId, out);
    }

}
