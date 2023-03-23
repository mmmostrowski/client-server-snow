package techbit.snow.proxy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.stream.SnowStream;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProxyServiceTest {

    @Mock
    private OutputStream out;
    @Mock
    private SnowStream snowStream;
    @Mock
    private SessionService session;
    @Mock
    private ObjectProvider<SnowStream> snowFactory;
    @Mock
    private ObjectProvider<PhpSnowConfig> configProvider;
    @Mock
    private Map<String, String> confMap;

    private ProxyService proxyService;

    @BeforeEach
    void setup() {
        proxyService = new ProxyService(session, snowFactory, configProvider);
    }


    @Test
    void givenNewSessionId_whenStream_thenStreamToANewStream() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.getObject("session-abc", confMap)).thenReturn(snowStream);

        proxyService.start("session-abc", out, confMap);

        verify(session).create("session-abc");
        verify(snowStream).startPhpApp();
        verify(snowStream).startConsumingSnowData();
        verify(snowStream).streamTo(out);
    }

    @Test
    void givenSameSessionId_whenStream_thenStreamToTheSameStream() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.getObject("session-abc", confMap)).thenReturn(snowStream);

        when(session.exists("session-abc")).thenReturn(false);
        proxyService.start("session-abc", out, confMap);

        when(session.exists("session-abc")).thenReturn(true);
        proxyService.start("session-abc", out, confMap);

        verify(snowStream, times(2)).streamTo(out);
    }

    @Test
    void whenSessionDoesNotExist_thenHasNoStream() {
        when(session.exists("session-abc")).thenReturn(false);

        assertFalse(proxyService.hasStream("session-abc"));
    }

    @Test
    void whenSessionExists_thenHasStream() {
        when(session.exists("session-abc")).thenReturn(true);

        assertTrue(proxyService.hasStream("session-abc"));
    }

    @Test
    void whenSessionDoesNotExist_thenProxyIsNotRunning() {
        when(session.exists("session-abc")).thenReturn(false);

        assertFalse(proxyService.isRunning("session-abc"));
    }

    @Test
    void whenStreamIsActive_thenProxyIsRunning() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.getObject("session-abc", confMap)).thenReturn(snowStream);
        when(snowStream.isActive()).thenReturn(true);
        proxyService.start("session-abc", out, confMap);
        when(session.exists("session-abc")).thenReturn(true);

        assertTrue(proxyService.isRunning("session-abc"));
    }

    @Test
    void whenNoActiveStream_thenProxyIsNotRunning() {
        assertFalse(proxyService.isRunning("session-abc"));
    }

    @Test
    void whenStopProxy_thenDeleteSession() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.getObject("session-abc", confMap)).thenReturn(snowStream);
        proxyService.start("session-abc", out, confMap);
        when(session.exists("session-abc")).thenReturn(true);

        proxyService.stop("session-abc");

        verify(session).delete("session-abc");
    }

    @Test
    void whenStopProxy_thenStopStream() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.getObject("session-abc", confMap)).thenReturn(snowStream);
        proxyService.start("session-abc", out, confMap);
        when(session.exists("session-abc")).thenReturn(true);

        proxyService.stop("session-abc");

        verify(snowStream).stop();
    }

    @Test
    void whenStopNonExistingSession_noErrorOccurs() {
        assertDoesNotThrow(() -> proxyService.stop("session-abc"));
    }

}