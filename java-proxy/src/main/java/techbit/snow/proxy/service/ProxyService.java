package techbit.snow.proxy.service;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.model.SnowStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Component
public class ProxyService {

    private final Logger logger = LogManager.getLogger(ProxyService.class);

    @Autowired
    private SessionService session;

    @Autowired
    private ObjectProvider<SnowStream> snowFactory;

    private final Map<String, SnowStream> streams = Maps.newHashMap();

    public void stream(String sessionId, OutputStream out, Map<String, String> confMap) throws IOException, InterruptedException {
        snowStream(sessionId, confMap).streamTo(out);
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

    private synchronized SnowStream snowStream(String sessionId, Map<String, String> confMap) throws IOException, InterruptedException {
        if (session.exists(sessionId)) {
            logger.debug(() -> String.format("snowStream( %s ) | Returning existing stream", sessionId));
            SnowStream stream = streams.get(sessionId);
            stream.ensureConfigCompatible(confMap);
            return stream;
        }
        logger.debug(() -> String.format("snowStream( %s ) | Creating new stream | %s", sessionId, confMap.toString()));
        SnowStream snow = snowFactory.getObject(sessionId, confMap);
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

    public boolean hasStream(String sessionId) {
        return session.exists(sessionId);
    }

    public boolean isRunning(String sessionId) {
        return session.exists(sessionId) && streams.get(sessionId).isActive();
    }

}
