package techbit.snow.proxy.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.stream.SnowStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProxyServiceTest {

    @Mock
    private SessionService session;

    @Mock
    private ObjectProvider<SnowStream> snowFactory;

    @Mock
    private OutputStream out;

    @Mock
    private SnowStream snowStream;

    @Mock
    private ObjectProvider<PhpSnowConfig> configProvider;

    private Map<String, String> confMap;

    private ProxyService proxyService;

    @BeforeEach
    void setup() {
        proxyService = new ProxyService(snowFactory, configProvider, session);
        confMap = new HashMap<>();
    }

    @Test
    void givenNewSessionId_whenStream_thenStreamToANewSnowStream() throws IOException, InterruptedException {
        when(snowFactory.getObject("session-abc", confMap)).thenReturn(snowStream);

        proxyService.stream("session-abc", out, confMap);

        verify(session).create("session-abc");
        verify(snowStream).startPhpApp();
        verify(snowStream).startConsumingSnowData();
        verify(snowStream).streamTo(out);
    }

    @Test
    void givenSameSessionId_whenStream_thenStreamToSameSnowStream() throws IOException, InterruptedException {
        when(snowFactory.getObject("session-abc", confMap)).thenReturn(snowStream);

        proxyService.stream("session-abc", out, confMap);

        when(session.exists("session-abc")).thenReturn(true);

        proxyService.stream("session-abc", out, confMap);

        verify(snowStream, times(2)).streamTo(out);
    }

    @Test
    void whenSessionExists_thenHasStream() {
        when(session.exists("session-abc")).thenReturn(true);

        Assertions.assertTrue(proxyService.hasStream("session-abc"));
    }

    @Test
    void whenSessionDoesNotExist_thenHasNoStream() {
        when(session.exists("session-abc")).thenReturn(false);

        Assertions.assertFalse(proxyService.hasStream("session-abc"));
    }

    @Test
    void whenSessionExistsAndThereIsActiveStream_thenIsRunning() throws IOException, InterruptedException {
        when(snowFactory.getObject("session-abc", confMap)).thenReturn(snowStream);
        when(snowStream.isActive()).thenReturn(true);

        proxyService.stream("session-abc", out, confMap);

        when(session.exists("session-abc")).thenReturn(true);

        Assertions.assertTrue(proxyService.isRunning("session-abc"));
    }

    @Test
    void whenSessionNotExists_thenIsNotRunning() {
        when(session.exists("session-abc")).thenReturn(false);

        Assertions.assertFalse(proxyService.isRunning("session-abc"));
    }

    @Test
    void whenNoActiveStream_thenIsNotRunning() {
        Assertions.assertFalse(proxyService.isRunning("session-abc"));
    }

    @Test
    void whenStopStream_thenSessionIsDeleted() throws IOException, InterruptedException {
        when(snowFactory.getObject("session-abc", confMap)).thenReturn(snowStream);
        proxyService.stream("session-abc", out, confMap);
        when(session.exists("session-abc")).thenReturn(true);

        proxyService.stopStream("session-abc");

        verify(session).delete("session-abc");
    }

    @Test
    void whenStopStream_thenStopPhpApp() throws IOException, InterruptedException {
        when(snowFactory.getObject("session-abc", confMap)).thenReturn(snowStream);
        proxyService.stream("session-abc", out, confMap);
        when(session.exists("session-abc")).thenReturn(true);

        proxyService.stopStream("session-abc");

        verify(snowStream).stop();
    }

    @Test
    void whenStopNonExistingStream_noErrorOccurs() throws IOException, InterruptedException {
        Assertions.assertDoesNotThrow(() -> proxyService.stopStream("session-abc"));
    }


}