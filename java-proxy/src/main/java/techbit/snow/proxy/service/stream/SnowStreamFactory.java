package techbit.snow.proxy.service.stream;

import java.util.Map;

public interface SnowStreamFactory {

    SnowStream create(String sessionId, Map<String, String> config);

}
