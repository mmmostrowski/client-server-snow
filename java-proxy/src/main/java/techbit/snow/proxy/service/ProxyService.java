package techbit.snow.proxy.service;

import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.model.NamedPipe;
import techbit.snow.proxy.model.SnowStream;
import techbit.snow.proxy.model.SnowStreamFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Component
public class ProxyService {

    private final Logger logger = LogManager.getLogger(ProxyService.class);

    @Autowired
    private SessionService session;

    @Autowired
    private SnowStreamFactory snowStreamFactory;

    private final Map<String, SnowStream> streams = Maps.newHashMap();

    public void stream(String sessionId, OutputStream out) throws IOException, InterruptedException {
        snowStream(sessionId).streamTo(out);
    }

    public void stopStream(String sessionId) {
        if (!session.exists(sessionId)) {
            logger.debug(() -> String.format("stopStream( %s ) | Nothing to stop!", sessionId));
            return;
        }
        logger.debug(() -> String.format("stopStream( %s ) | Stopping PhpSnow App", sessionId));
        streams.get(sessionId).stopPhpApp();
        removeStream(sessionId);
    }

    private synchronized SnowStream snowStream(String sessionId) throws IOException, InterruptedException {
        if (session.exists(sessionId)) {
            logger.debug(() -> String.format("snowStream( %s ) | Returning existing stream", sessionId));
            return streams.get(sessionId);
        }
        logger.debug(() -> String.format("snowStream( %s ) | Creating new stream", sessionId));
        SnowStream snow = snowStreamFactory.create(sessionId);
        snow.startPhpApp();
        snow.startConsumingSnowData();
        streams.put(sessionId, snow);
        session.create(sessionId);
        return snow;
    }

    private synchronized void removeStream(String sessionId) {
        if (!session.exists(sessionId)) {
            return;
        }

        logger.debug(() -> String.format("stopStream( %s ) | Removing stream", sessionId));
        streams.remove(sessionId);
        session.delete(sessionId);
    }
}
