package techbit.snow.proxy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    private SessionService sessions;

    @BeforeEach
    void setup() {
        sessions = new SessionService();
    }

    @Test
    void whenSessionCreated_thenSessionExists() {
        sessions.create("xyz");

        assertTrue(sessions.exists("xyz"));
    }

    @Test
    void whenSessionNotCreated_thenSessionNotExists() {
        assertFalse(sessions.exists("xyz"));
    }

    @Test
    void whenOtherSessionCreated_thenOurSessionNotExists() {
        sessions.create("other");

        assertFalse(sessions.exists("xyz"));
    }

    @Test
    void whenSessionDeleted_thenSessionNotExists() {
        sessions.create("xyz");
        sessions.delete("xyz");

        assertFalse(sessions.exists("xyz"));
    }

    @Test
    void whenNonExistentSessionDeleted_thenExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class,
            () -> sessions.delete("not-exists"));
    }

    @Test
    void whenCreatingSameSessionTwice_thenExceptionIsThrown() {
        sessions.create("xyz");

        assertThrows(IllegalArgumentException.class,
                () -> sessions.create("xyz"));
    }

    @Test
    void whenMultipleSessionsCreated_thenBothExists() {
        sessions.create("abc");
        sessions.create("xyz");

        assertTrue(sessions.exists("abc"));
        assertTrue(sessions.exists("xyz"));
    }

    @Test
    void whenEmptySessionName_thenExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> sessions.create(""));
    }

    @Test
    void whenValidSessionName_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> sessions.create("session"));
        assertDoesNotThrow(() -> sessions.create("session-abc"));
        assertDoesNotThrow(() -> sessions.create("123-other"));
        assertDoesNotThrow(() -> sessions.create("1-2-3"));
    }

    @Test
    void whenInvalidSessionName_thenExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> sessions.create("!"));
        assertThrows(IllegalArgumentException.class, () -> sessions.create("@"));
        assertThrows(IllegalArgumentException.class, () -> sessions.create("with space"));
        assertThrows(IllegalArgumentException.class, () -> sessions.create("with_underscore"));
        assertThrows(IllegalArgumentException.class, () -> sessions.create("with_under"));
        assertThrows(IllegalArgumentException.class, () -> sessions.create("UpperCase"));
    }
}