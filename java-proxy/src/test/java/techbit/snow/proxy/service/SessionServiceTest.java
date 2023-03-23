package techbit.snow.proxy.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    @Test
    void whenSessionCreated_thenSessionExists() {
        SessionService sessions = new SessionService();

        sessions.create("xyz");

        assertTrue(sessions.exists("xyz"));
    }

    @Test
    void whenSessionNotCreated_thenSessionNotExists() {
        SessionService sessions = new SessionService();

        assertFalse(sessions.exists("xyz"));
    }

    @Test
    void whenOtherSessionCreated_thenOurSessionNotExists() {
        SessionService sessions = new SessionService();

        sessions.create("other");

        assertFalse(sessions.exists("xyz"));
    }

    @Test
    void whenSessionDeleted_thenSessionNotExists() {
        SessionService sessions = new SessionService();

        sessions.create("xyz");
        sessions.delete("xyz");

        assertFalse(sessions.exists("xyz"));
    }

    @Test
    void whenNonExistentSessionDeleted_thenExceptionIsThrown() {
        SessionService sessions = new SessionService();

        assertThrows(IllegalArgumentException.class,
            () -> sessions.delete("not-exists"));
    }

    @Test
    void whenCreatingSameSessionTwice_thenExceptionIsThrown() {
        SessionService sessions = new SessionService();

        sessions.create("xyz");

        assertThrows(IllegalArgumentException.class,
                () -> sessions.create("xyz"));
    }

    @Test
    void whenMultipleSessionsCreated_thenBothExists() {
        SessionService sessions = new SessionService();

        sessions.create("abc");
        sessions.create("xyz");

        assertTrue(sessions.exists("abc"));
        assertTrue(sessions.exists("xyz"));
    }

    @Test
    void whenEmptySessionName_thenExceptionIsThrown() {
        SessionService sessions = new SessionService();

        assertThrows(IllegalArgumentException.class, () -> sessions.create(""));
    }

    @Test
    void whenInvalidSessionName_thenExceptionIsThrown() {
        SessionService sessions = new SessionService();

        assertThrows(IllegalArgumentException.class, () -> sessions.create("!"));
        assertThrows(IllegalArgumentException.class, () -> sessions.create("@"));
        assertThrows(IllegalArgumentException.class, () -> sessions.create("with space"));
        assertThrows(IllegalArgumentException.class, () -> sessions.create("with_underscore"));
        assertThrows(IllegalArgumentException.class, () -> sessions.create("with_under"));
        assertThrows(IllegalArgumentException.class, () -> sessions.create("UpperCase"));
    }
}