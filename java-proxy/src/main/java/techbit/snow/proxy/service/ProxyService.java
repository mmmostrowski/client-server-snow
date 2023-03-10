package techbit.snow.proxy.service;

import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import techbit.snow.proxy.service.stream.SnowStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Service
@Log4j2
public class ProxyService {

    @Autowired
    private SessionService session;

    @Autowired
    @Qualifier("snowStream.create")
    private ObjectProvider<SnowStream> snowFactory;

    private final Map<String, SnowStream> streams = Maps.newHashMap();

    public void stream(String sessionId, OutputStream out, Map<String, String> confMap) throws IOException, InterruptedException {
        snowStream(sessionId, confMap).streamTo(out);
    }

    public void stopStream(String sessionId) throws IOException {
        if (!session.exists(sessionId)) {
            log.debug("stopStream( {} ) | Nothing to stop!", sessionId);
            return;
        }
        log.debug("stopStream( {} ) | Stopping PhpSnow App", sessionId);
        streams.get(sessionId).stopPhpApp();
        removeStream(sessionId);
    }

    private synchronized SnowStream snowStream(String sessionId, Map<String, String> confMap) throws IOException, InterruptedException {
        if (session.exists(sessionId)) {
            log.debug("snowStream( {} ) | Returning existing stream", sessionId);
            SnowStream stream = streams.get(sessionId);
            stream.ensureConfigCompatible(confMap);
            return stream;
        }
        log.debug("snowStream( {} ) | Creating new stream | {}", sessionId, confMap);
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

        log.debug("stopStream( {} ) | Removing stream", sessionId);
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
