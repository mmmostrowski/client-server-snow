package techbit.snow.proxy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.exception.InvalidSessionException;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfigConverter;
import techbit.snow.proxy.service.stream.SnowStream;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;
import techbit.snow.proxy.service.stream.SnowStreamFactory;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("ALL")
@ExtendWith(MockitoExtension.class)
class ProxyServiceTest {

    @Mock
    private OutputStream out;
    @Mock
    private SnowStream snowStream;
    @Mock
    private SessionService session;
    @Mock
    private StreamEncoder streamEncoder;
    @Mock
    private Map<String, String> confMap;
    @Mock
    private SnowStreamFactory snowFactory;
    @Spy
    private Map<String, SnowStream> streams = new HashMap<>();
    @Mock
    private PhpSnowConfigConverter configProvider;
    @Mock
    private SnowStream.SnowDataClient snowDataClient;
    @Mock
    private SnowStream.SnowStreamFinishedEvent streamFinishedEvent;
    private ProxyServiceImpl proxyService;
    private ProxyServiceImpl proxyServiceSpyStreams;

    @BeforeEach
    void setup() {
        proxyService = new ProxyServiceImpl(session, snowFactory, configProvider);
        proxyServiceSpyStreams = new ProxyServiceImpl(session, snowFactory, configProvider, streams);
    }

    @Test
    void whenStartStream_thenStartPhpAppAndStartConsumingData() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", confMap)).thenReturn(snowStream);

        proxyService.startSession("session-abc", confMap);

        verify(session).create("session-abc");

        InOrder inOrder = inOrder(snowStream);
        inOrder.verify(snowStream).startPhpApp();
        inOrder.verify(snowStream).startConsumingSnowData();
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
    void givenCustomizations_whenStream_thenStreamToANewStream() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create(eq("session-abc"), eq(Collections.emptyMap()))).thenReturn(snowStream);

        proxyService.streamSessionTo("session-abc", out, streamEncoder, snowDataClient);

        verify(session).create("session-abc");
        verify(snowStream).startPhpApp();
        verify(snowStream).startConsumingSnowData();
        verify(snowStream).streamTo(out, streamEncoder, snowDataClient);
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

    @Test
    void givenValidSession_whenAskingForDetails_thenProvideThemFromProxyService() {

        Map<String, Object> expected = Collections.emptyMap();
        when(session.exists("session-abc")).thenReturn(true);
        when(streams.get("session-abc")).thenReturn(snowStream);
        when(snowStream.configDetails(any())).thenReturn(expected);

        Map<String, Object> details = proxyServiceSpyStreams.sessionDetails("session-abc");

        assertSame(expected, details);
    }

    @Test
    void givenInvalidSession_whenAskingForDetails_thenThrowException() {
        assertThrows(InvalidSessionException.class,
                () -> proxyService.sessionDetails("session-abc"));
    }

    @Test
    void whenSnowStreamFinishEventOccurs_thenStopSession() throws IOException, InterruptedException {
        when(session.exists("session-abc")).thenReturn(true);
        when(streamFinishedEvent.getSessionId()).thenReturn("session-abc");
        when(streams.get("session-abc")).thenReturn(snowStream);

        proxyServiceSpyStreams.onApplicationEvent(streamFinishedEvent);

        verify(snowStream).stop();
    }

}