package techbit.snow.proxy.service;

import techbit.snow.proxy.service.stream.SnowStream;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface ProxyService {

    void startSession(String sessionId, Map<String, String> confMap)
            throws IOException;

    void streamSessionTo(String sessionId, OutputStream out, StreamEncoder encoder, SnowStream.Customizations customs)
            throws IOException, InterruptedException, SnowStream.ConsumerThreadException;

    void streamSessionTo(String sessionId, OutputStream out, StreamEncoder encoder, Map<String, String> confMap)
            throws IOException, InterruptedException, SnowStream.ConsumerThreadException;

    void stopSession(String sessionId) throws IOException, InterruptedException;

    Map<String, Object> sessionDetails(String sessionId);

    boolean hasSession(String sessionId);

    boolean isSessionRunning(String sessionId);

}
