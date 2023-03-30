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
import techbit.snow.proxy.service.stream.SnowStream;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;
import techbit.snow.proxy.service.stream.SnowStreamFactory;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

@Log4j2
@Service
@Primary
public class ProxyServiceImpl implements ProxyService, ApplicationListener<SnowStream.SnowStreamFinishedEvent> {

    private final Map<String, SnowStream> streams = Maps.newHashMap();
    private final PhpSnowConfigConverter configConverter;
    private final SnowStreamFactory snowStreamProvider;
    private final SessionService session;

    public ProxyServiceImpl(
            @Autowired SessionService session,
            @Autowired SnowStreamFactory snowStreamProvider,
            @Autowired PhpSnowConfigConverter configConverter
    ) {
        this.snowStreamProvider = snowStreamProvider;
        this.session = session;
        this.configConverter = configConverter;
    }

    @Override
    public void startSession(String sessionId, Map<String, String> confMap) throws IOException {
        snowStream(sessionId, confMap);
    }

    @Override
    public void streamSessionTo(String sessionId, OutputStream out, StreamEncoder encoder, SnowStream.Customizations customs)
            throws IOException, InterruptedException, ConsumerThreadException
    {
        snowStream(sessionId, Collections.emptyMap()).streamTo(out, encoder, customs);
    }

    @Override
    public void streamSessionTo(String sessionId, OutputStream out, StreamEncoder encoder, Map<String, String> confMap)
            throws IOException, InterruptedException, ConsumerThreadException
    {
        snowStream(sessionId, confMap).streamTo(out, encoder);
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
    public synchronized Map<String, Object> sessionDetails(String sessionId) {
        if (!session.exists(sessionId)) {
            throw new InvalidSessionException("Unknown snow streaming session:" + sessionId);
        }

        final SnowStream stream = streams.get(sessionId);
        return stream.configDetails(configConverter);
    }

    private synchronized SnowStream snowStream(String sessionId, Map<String, String> confMap) throws IOException {
        if (session.exists(sessionId)) {
            log.debug("snowStream( {} ) | Returning existing stream", sessionId);
            final SnowStream stream = streams.get(sessionId);
            if (!confMap.isEmpty()) {
                stream.ensureCompatibleWithConfig(configConverter.fromMap(confMap));
            }
            return stream;
        }
        log.debug("snowStream( {} ) | Creating new stream | {}", sessionId, confMap);
        final SnowStream snow = snowStreamProvider.create(sessionId, confMap);
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
