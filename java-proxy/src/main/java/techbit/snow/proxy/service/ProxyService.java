package techbit.snow.proxy.service;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.model.SnowStream;
import techbit.snow.proxy.model.SnowStreamFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Component
public class ProxyService {

    @Autowired
    private SessionService session;

    @Autowired
    private SnowStreamFactory snowStreamFactory;

    private final Map<String, SnowStream> streams = Maps.newHashMap();

    public void stream(String sessionId, OutputStream out) throws IOException {
        snowStream(sessionId).streamTo(out);
    }

    private synchronized SnowStream snowStream(String sessionId) {
        if (session.exists(sessionId)) {
            return streams.get(sessionId);
        }
        SnowStream snow = snowStreamFactory.create(sessionId);
        snow.startPhpApp();
        streams.put(sessionId, snow);
        session.create(sessionId);
        return snow;
    }
}
