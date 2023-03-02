package techbit.snow.proxy.model;

import org.springframework.stereotype.Component;

@Component
public class SnowStreamFactory {

    public SnowStream create(String sessionId) {
        return SnowStream.of(sessionId);
    }

}
