package techbit.snow.proxy.service;

import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.stream.SnowStream;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Service
@Log4j2
public class ProxyService {

    private final Map<String, SnowStream> streams = Maps.newHashMap();
    private final ObjectProvider<PhpSnowConfig> configProvider;
    private final ObjectProvider<SnowStream> snowFactory;
    private final SessionService session;



    public ProxyService(
            @Autowired @Qualifier("snowStream.create") ObjectProvider<SnowStream> snowFactory,
            @Autowired @Qualifier("phpsnowConfig.create") ObjectProvider<PhpSnowConfig> configProvider,
            @Autowired SessionService session
    ) {
        this.snowFactory = snowFactory;
        this.session = session;
        this.configProvider = configProvider;
    }

    public void stream(String sessionId, OutputStream out, Map<String, String> confMap) throws IOException, InterruptedException, ConsumerThreadException {
        snowStream(sessionId, confMap).streamTo(out);
    }

    public void stopStream(String sessionId) throws IOException, InterruptedException {
        if (!session.exists(sessionId)) {
            log.debug("stopStream( {} ) | Nothing to stop!", sessionId);
            return;
        }
        log.debug("stopStream( {} ) | Stopping PhpSnow App", sessionId);
        SnowStream snowStream = streams.get(sessionId);
        removeStream(sessionId);
        snowStream.stop();
    }

    private synchronized SnowStream snowStream(String sessionId, Map<String, String> confMap) throws IOException, InterruptedException {
        if (session.exists(sessionId)) {
            log.debug("snowStream( {} ) | Returning existing stream", sessionId);
            SnowStream stream = streams.get(sessionId);
            stream.ensureCompatibleWithConfig(configProvider.getObject(confMap));
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
        session.delete(sessionId);
        streams.remove(sessionId);
    }

    public boolean hasStream(String sessionId) {
        return session.exists(sessionId);
    }

    public boolean isRunning(String sessionId) {
        return session.exists(sessionId) && streams.get(sessionId).isActive();
    }

}
