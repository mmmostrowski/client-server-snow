package techbit.snow.proxy.service;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import techbit.snow.proxy.exception.InvalidSessionException;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfigConverter;
import techbit.snow.proxy.service.stream.snow.SnowStream;
import techbit.snow.proxy.service.stream.snow.SnowStream.ConsumerThreadException;
import techbit.snow.proxy.service.stream.snow.SnowStreamClient;
import techbit.snow.proxy.service.stream.snow.SnowStreamFactory;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;
import techbit.snow.proxy.service.stream.snow.SnowStreamSimpleClient;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

@Log4j2
@Service
@Primary
public class ProxyServiceImpl implements ProxyService, ApplicationListener<SnowStream.SnowStreamFinishedEvent> {

    private final PhpSnowConfigConverter configConverter;
    private final SnowStreamFactory snowStreamProvider;
    private final Map<String, SnowStream> streams;
    private final SessionService session;

    @Autowired
    public ProxyServiceImpl(
            SessionService session,
            SnowStreamFactory snowStreamProvider,
            PhpSnowConfigConverter configConverter
    ) {
        this(session, snowStreamProvider, configConverter, Maps.newHashMap());
    }

    public ProxyServiceImpl(
            SessionService session,
            SnowStreamFactory snowStreamProvider,
            PhpSnowConfigConverter configConverter,
            Map<String, SnowStream> streams
    ) {
        this.snowStreamProvider = snowStreamProvider;
        this.session = session;
        this.configConverter = configConverter;
        this.streams = streams;
    }

    @Override
    public void startSession(String sessionId, Map<String, String> config) throws IOException {
        snowStream(sessionId, config);
    }

    @Override
    public void streamSessionTo(String sessionId, OutputStream out, StreamEncoder encoder, Map<String, String> config)
            throws IOException, InterruptedException, ConsumerThreadException
    {
        snowStream(sessionId, config).streamTo(new SnowStreamSimpleClient(encoder, out));
    }

    @Override
    public void streamSessionTo(String sessionId, SnowStreamClient client)
            throws IOException, InterruptedException, ConsumerThreadException
    {
        snowStream(sessionId, Collections.emptyMap()).streamTo(client);
    }

    @Override
    public void stopSession(String sessionId) throws IOException, InterruptedException {
        if (!session.exists(sessionId)) {
            log.debug("stopStream( {} ) | Nothing to stop!", sessionId);
            return;
        }
        log.debug("stopStream( {} ) | Stopping PhpSnow App", sessionId);
        final SnowStream snowStream = streams.get(sessionId);
        removeStream(sessionId);
        snowStream.stop();
    }

    @Override
    public Map<String, Object> sessionDetails(String sessionId) {
        if (!session.exists(sessionId)) {
            throw new InvalidSessionException("Unknown snow streaming session:" + sessionId);
        }
        final SnowStream stream = streams.get(sessionId);
        return configConverter.toMap(stream.config());
    }

    private synchronized SnowStream snowStream(String sessionId, Map<String, String> config) throws IOException {
        if (session.exists(sessionId)) {
            log.debug("snowStream( {} ) | Returning existing stream", sessionId);
            final SnowStream stream = streams.get(sessionId);
            if (!config.isEmpty()) {
                stream.ensureCompatibleWithConfig(configConverter.fromMap(config));
            }
            return stream;
        }
        log.debug("snowStream( {} ) | Creating new stream | {}", sessionId, config);
        final SnowStream snow = snowStreamProvider.create(sessionId, config);
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

    @Override
    public boolean hasSession(String sessionId) {
        return session.exists(sessionId);
    }

    @Override
    public boolean isSessionRunning(String sessionId) {
        return session.exists(sessionId) && streams.get(sessionId).isActive();
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(SnowStream.SnowStreamFinishedEvent event) {
        stopSession(event.getSessionId());
    }
}
