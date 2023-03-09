package techbit.snow.proxy.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    @Test
    void whenSessionCreated_sessionExists() {
        SessionService sessions = new SessionService();

        sessions.create("xyz");

        assertTrue(sessions.exists("xyz"));
    }

    @Test
    void whenSessionNotCreated_sessionNotExists() {
        SessionService sessions = new SessionService();

        assertFalse(sessions.exists("xyz"));
    }

    @Test
    void whenOtherSessionCreated_ourSessionNotExists() {
        SessionService sessions = new SessionService();

        sessions.create("other");

        assertFalse(sessions.exists("xyz"));
    }

    @Test
    void whenSessionDeleted_sessionNotExists() {
        SessionService sessions = new SessionService();

        sessions.create("xyz");
        sessions.delete("xyz");

        assertFalse(sessions.exists("xyz"));
    }

    @Test
    void whenNonExistentSessionDeleted_exceptionIsThrown() {
        SessionService sessions = new SessionService();

        assertThrows(Exception.class,
            () -> sessions.delete("not-exists"));
    }

    @Test
    void whenCreateSameSessionTwice_exceptionIsThrown() {
        SessionService sessions = new SessionService();

        sessions.create("xyz");

        assertThrows(Exception.class,
                () -> sessions.create("xyz"));
    }

    @Test
    void whenMultipleSessionsCreated_theyAreNotInterfering() {
        SessionService sessions = new SessionService();

        sessions.create("abc");
        sessions.create("xyz");

        assertTrue(sessions.exists("abc"));
        assertTrue(sessions.exists("xyz"));
    }

    @Test
    void whenEmptySessionName_exceptionIsThrown() {
        SessionService sessions = new SessionService();

        assertThrows(Exception.class, () -> sessions.create(""));
    }

    @Test
    void whenInvalidSessionName_exceptionIsThrown() {
        SessionService sessions = new SessionService();

        assertThrows(Exception.class, () -> sessions.create("!"));
        assertThrows(Exception.class, () -> sessions.create("@"));
        assertThrows(Exception.class, () -> sessions.create("with space"));
        assertThrows(Exception.class, () -> sessions.create("with_underscore"));
        assertThrows(Exception.class, () -> sessions.create("with_under"));
        assertThrows(Exception.class, () -> sessions.create("UpperCase"));
    }
}