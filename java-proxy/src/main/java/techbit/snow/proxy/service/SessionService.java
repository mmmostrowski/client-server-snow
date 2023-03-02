package techbit.snow.proxy.service;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SessionService {

    private final Set<String> sessions = new HashSet<>();

    public boolean exists(String sessionId) {
        return sessions.contains(sessionId);
    }

    public void create(String sessionId) {
        sessions.add(sessionId);
    }

    public void delete(String sessionId) {
        sessions.remove(sessionId);
    }
}
