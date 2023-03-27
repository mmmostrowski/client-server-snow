package techbit.snow.proxy.service;

import techbit.snow.proxy.service.stream.SnowStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface ProxyService {

    void startSession(String sessionId, OutputStream out, String outputType, Map<String, String> confMap)
            throws IOException, InterruptedException, SnowStream.ConsumerThreadException;

    void stopSession(String sessionId) throws IOException, InterruptedException;

    boolean hasSession(String sessionId);

    boolean isSessionRunning(String sessionId);

}
