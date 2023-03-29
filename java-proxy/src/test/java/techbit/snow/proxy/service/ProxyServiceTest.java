package techbit.snow.proxy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfigConverter;
import techbit.snow.proxy.service.stream.SnowStream;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;
import techbit.snow.proxy.service.stream.SnowStreamFactory;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;
import techbit.snow.proxy.service.stream.encoding.StreamEncoderFactory;

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
    private SnowStreamFactory snowFactory;
    @Mock
    private PhpSnowConfigConverter configProvider;
    @Mock
    private Map<String, String> confMap;
    @Mock
    private StreamEncoderFactory streamEncoderFactory;
    @Mock
    private StreamEncoder streamEncoder;


    private ProxyServiceImpl proxyService;

    @BeforeEach
    void setup() {
        proxyService = new ProxyServiceImpl(session, snowFactory, configProvider);
    }


    @Test
    void givenNewSessionId_whenStream_thenStreamToANewStream() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", confMap)).thenReturn(snowStream);

        proxyService.streamSessionTo("session-abc", out, streamEncoder, confMap);

        verify(session).create("session-abc");
        verify(snowStream).startPhpApp();
        verify(snowStream).startConsumingSnowData();
        verify(snowStream).streamTo(out, streamEncoder);
    }

    @Test
    void givenSameSessionId_whenStream_thenStreamToTheSameStream() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", confMap)).thenReturn(snowStream);

        when(session.exists("session-abc")).thenReturn(false);
        proxyService.streamSessionTo("session-abc", out, streamEncoder, confMap);

        when(session.exists("session-abc")).thenReturn(true);
        proxyService.streamSessionTo("session-abc", out, streamEncoder, confMap);

        verify(snowStream, times(2)).streamTo(out, streamEncoder);
    }

    @Test
    void whenSessionDoesNotExist_thenHasNoStream() {
        when(session.exists("session-abc")).thenReturn(false);

        assertFalse(proxyService.hasSession("session-abc"));
    }

    @Test
    void whenSessionExists_thenHasStream() {
        when(session.exists("session-abc")).thenReturn(true);

        assertTrue(proxyService.hasSession("session-abc"));
    }

    @Test
    void whenSessionDoesNotExist_thenProxyIsNotRunning() {
        when(session.exists("session-abc")).thenReturn(false);

        assertFalse(proxyService.isSessionRunning("session-abc"));
    }

    @Test
    void whenStreamIsActive_thenProxyIsRunning() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", confMap)).thenReturn(snowStream);
        when(snowStream.isActive()).thenReturn(true);
        proxyService.streamSessionTo("session-abc", out, streamEncoder, confMap);
        when(session.exists("session-abc")).thenReturn(true);

        assertTrue(proxyService.isSessionRunning("session-abc"));
    }

    @Test
    void whenNoActiveStream_thenProxyIsNotRunning() {
        assertFalse(proxyService.isSessionRunning("session-abc"));
    }

    @Test
    void whenStopProxy_thenDeleteSession() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", confMap)).thenReturn(snowStream);
        proxyService.streamSessionTo("session-abc", out, streamEncoder, confMap);
        when(session.exists("session-abc")).thenReturn(true);

        proxyService.stopSession("session-abc");

        verify(session).delete("session-abc");
    }

    @Test
    void whenStopProxy_thenStopStream() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", confMap)).thenReturn(snowStream);
        proxyService.streamSessionTo("session-abc", out, streamEncoder, confMap);
        when(session.exists("session-abc")).thenReturn(true);

        proxyService.stopSession("session-abc");

        verify(snowStream).stop();
    }

    @Test
    void whenStopNonExistingSession_noErrorOccurs() {
        assertDoesNotThrow(() -> proxyService.stopSession("session-abc"));
    }

}