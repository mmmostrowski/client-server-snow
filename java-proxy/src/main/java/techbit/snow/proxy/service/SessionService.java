package techbit.snow.proxy.service;

import org.springframework.stereotype.Service;
import techbit.snow.proxy.exception.InvalidSessionException;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class SessionService {

    private final Pattern sessionIdValidator = Pattern.compile("^[a-z0-9-]+$");
    private final Set<String> sessions = new HashSet<>();


    public void create(String sessionId) {
        validate(sessionId);
        if (sessions.contains(sessionId)) {
            throw new InvalidSessionException("You cannot create session with same id twice: " + sessionId);
        }
        sessions.add(sessionId);
    }

    public boolean exists(String sessionId) {
        return sessions.contains(sessionId);
    }

    public void delete(String sessionId) {
        if (!sessions.contains(sessionId)) {
            throw new InvalidSessionException("Session does not exist: " + sessionId);
        }
        sessions.remove(sessionId);
    }

    private void validate(String sessionId) {
        if (!sessionIdValidator.matcher(sessionId).matches()) {
            throw new InvalidSessionException("Invalid session id: " + sessionId
                    + ". Only lowercase alphanumeric characters and dashes are allowed!");
        }
    }
}
