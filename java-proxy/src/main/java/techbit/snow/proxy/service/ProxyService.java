package techbit.snow.proxy.service;

import techbit.snow.proxy.service.stream.snow.SnowStream;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;
import techbit.snow.proxy.service.stream.snow.SnowStreamClient;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface ProxyService {

    void startSession(String sessionId, Map<String, String> config)
            throws IOException;

    void streamSessionTo(String sessionId, OutputStream out, StreamEncoder encoder, Map<String, String> config)
            throws IOException, InterruptedException, SnowStream.ConsumerThreadException;

    void streamSessionTo(String sessionId, SnowStreamClient client)
            throws IOException, InterruptedException, SnowStream.ConsumerThreadException;

    void stopSession(String sessionId)
            throws IOException, InterruptedException;

    boolean hasSession(String sessionId);

    Map<String, Object> sessionDetails(String sessionId);

    boolean isSessionRunning(String sessionId);

}
