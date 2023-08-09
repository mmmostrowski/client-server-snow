package techbit.snow.proxy.proxy;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import techbit.snow.proxy.config.PhpSnowConfigConverter;
import techbit.snow.proxy.error.InvalidSessionException;
import techbit.snow.proxy.snow.stream.SnowStream;
import techbit.snow.proxy.snow.stream.SnowStream.ConsumerThreadException;
import techbit.snow.proxy.snow.stream.SnowStream.SnowStreamFinishedEvent;
import techbit.snow.proxy.snow.stream.SnowStreamClient;
import techbit.snow.proxy.snow.stream.SnowStreamFactory;
import techbit.snow.proxy.snow.stream.SnowStreamSimpleClient;
import techbit.snow.proxy.snow.transcoding.StreamEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

@Log4j2
@Service
public final class ProxyService implements ApplicationListener<SnowStreamFinishedEvent> {

    private final Map<String, Object> sessionLocks = Maps.newConcurrentMap();
    private final PhpSnowConfigConverter configConverter;
    private final SnowStreamFactory snowStreamProvider;
    private final Map<String, SnowStream> streams;
    private final Session session;

    @Autowired
    public ProxyService(
            Session session,
            SnowStreamFactory snowStreamProvider,
            PhpSnowConfigConverter configConverter
    ) {
        this(session, snowStreamProvider, configConverter, Maps.newHashMap());
    }

    public ProxyService(
            Session session,
            SnowStreamFactory snowStreamProvider,
            PhpSnowConfigConverter configConverter,
            Map<String, SnowStream> streams
    ) {
        this.snowStreamProvider = snowStreamProvider;
        this.session = session;
        this.configConverter = configConverter;
        this.streams = streams;
    }

    public void startSession(String sessionId, Map<String, String> config) throws IOException {
        snowStream(sessionId, config);
    }

    public void streamSessionTo(String sessionId, OutputStream out, StreamEncoder encoder, Map<String, String> config)
            throws IOException, InterruptedException, ConsumerThreadException
    {
        snowStream(sessionId, config).streamTo(new SnowStreamSimpleClient(encoder, out));
    }

    public void streamSessionTo(String sessionId, SnowStreamClient client)
            throws IOException, InterruptedException, ConsumerThreadException
    {
        snowStream(sessionId, Collections.emptyMap()).streamTo(client);
    }

    public void stopSession(String sessionId) throws IOException, InterruptedException {
        synchronized (sessionLock(sessionId)) {
            if (!session.exists(sessionId)) {
                log.debug("stopStream( {} ) | Nothing to stop!", sessionId);
                return;
            }
            log.debug("stopStream( {} ) | Stopping PhpSnow App", sessionId);
            final SnowStream snowStream = streams.get(sessionId);
            removeStream(sessionId);
            snowStream.stop();
        }
    }

    public Map<String, Object> sessionDetails(String sessionId) {
        synchronized (sessionLock(sessionId)) {
            if (!session.exists(sessionId)) {
                throw new InvalidSessionException("Unknown snow streaming session:" + sessionId);
            }
            final SnowStream stream = streams.get(sessionId);
            return configConverter.toMap(stream.config());
        }
    }

    private SnowStream snowStream(String sessionId, Map<String, String> config) throws IOException {
        synchronized (sessionLock(sessionId)) {
            if (session.exists(sessionId)) {
                return existingStream(sessionId, config);
            } else {
                return createStream(sessionId, config);
            }
        }
    }

    private SnowStream existingStream(String sessionId, Map<String, String> config) {
        log.debug("snowStream( {} ) | Returning existing stream", sessionId);
        SnowStream stream = streams.get(sessionId);
        if (!config.isEmpty()) {
            stream.ensureCompatibleWithConfig(sessionId, configConverter.fromMap(config));
        }
        return stream;
    }

    private SnowStream createStream(String sessionId, Map<String, String> config) throws IOException {
        log.debug("snowStream( {} ) | Creating new stream | {}", sessionId, config);
        SnowStream stream = snowStreamProvider.create(sessionId, config);
        stream.startPhpApp();
        stream.startConsumingSnowData();
        streams.put(sessionId, stream);
        session.create(sessionId);
        return stream;
    }

    private synchronized void removeStream(String sessionId) {
        log.debug("stopStream( {} ) | Removing stream", sessionId);
        session.delete(sessionId);
        streams.remove(sessionId);
    }

    public boolean hasSession(String sessionId) {
        synchronized (sessionLock(sessionId)) {
            return session.exists(sessionId);
        }
    }

    public boolean isSessionRunning(String sessionId) {
        synchronized (sessionLock(sessionId)) {
            return session.exists(sessionId) && streams.get(sessionId).isActive();
        }
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(SnowStreamFinishedEvent event) {
        if (session.exists(event.getSessionId())) {
            stopSession(event.getSessionId());
        }
    }

    private Object sessionLock(String sessionId) {
        return sessionLocks.computeIfAbsent(sessionId, k -> new Object());
    }
}
