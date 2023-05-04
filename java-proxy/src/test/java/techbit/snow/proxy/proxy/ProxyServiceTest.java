package techbit.snow.proxy.proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.config.PhpSnowConfig;
import techbit.snow.proxy.config.PhpSnowConfigConverter;
import techbit.snow.proxy.error.InvalidSessionException;
import techbit.snow.proxy.snow.stream.SnowStream;
import techbit.snow.proxy.snow.stream.SnowStream.ConsumerThreadException;
import techbit.snow.proxy.snow.stream.SnowStreamClient;
import techbit.snow.proxy.snow.stream.SnowStreamFactory;
import techbit.snow.proxy.snow.transcoding.StreamEncoder;

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
    private Session session;
    @Mock
    private StreamEncoder streamEncoder;
    @Mock
    private Map<String, String> configMap;
    @Mock
    private PhpSnowConfig config;
    @Mock
    private SnowStreamFactory snowFactory;
    @Spy
    private Map<String, SnowStream> streams = new HashMap<>();
    @Mock
    private PhpSnowConfigConverter configConverter;
    @Mock
    private SnowStreamClient snowDataClient;
    @Mock
    private SnowStream.SnowStreamFinishedEvent streamFinishedEvent;
    private ProxyService proxyService;
    private ProxyService proxyServiceSpyStreams;

    @BeforeEach
    void setup() {
        proxyService = new ProxyService(session, snowFactory, configConverter);
        proxyServiceSpyStreams = new ProxyService(session, snowFactory, configConverter, streams);
    }

    @Test
    void whenStartStream_thenStartPhpAppAndStartConsumingData() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", configMap)).thenReturn(snowStream);

        proxyService.startSession("session-abc", configMap);

        verify(session).create("session-abc");

        InOrder inOrder = inOrder(snowStream);
        inOrder.verify(snowStream).startPhpApp();
        inOrder.verify(snowStream).startConsumingSnowData();
    }

    @Test
    void givenNewSessionId_whenStream_thenStreamToANewStream() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", configMap)).thenReturn(snowStream);

        proxyService.streamSessionTo("session-abc", out, streamEncoder, configMap);

        verify(session).create("session-abc");
        verify(snowStream).startPhpApp();
        verify(snowStream).startConsumingSnowData();
        verify(snowStream).streamTo(any(SnowStreamClient.class));
    }

    @Test
    void givenSameSessionId_whenStream_thenStreamToTheSameStream() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", configMap)).thenReturn(snowStream);

        when(session.exists("session-abc")).thenReturn(false);
        proxyService.streamSessionTo("session-abc", out, streamEncoder, configMap);

        when(session.exists("session-abc")).thenReturn(true);
        proxyService.streamSessionTo("session-abc", out, streamEncoder, configMap);

        verify(snowStream, times(2)).streamTo(any(SnowStreamClient.class));
    }

    @Test
    void givenCustomClient_whenStream_thenStreamToANewStream() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create(eq("session-abc"), eq(Collections.emptyMap()))).thenReturn(snowStream);

        proxyService.streamSessionTo("session-abc", snowDataClient);

        verify(session).create("session-abc");
        verify(snowStream).startPhpApp();
        verify(snowStream).startConsumingSnowData();
        verify(snowStream).streamTo(snowDataClient);
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
    void whenSessionExistsButNotRunning_thenProxyIsNotRunning() {
        when(session.exists("session-abc")).thenReturn(true);
        when(streams.get("session-abc")).thenReturn(snowStream);
        when(snowStream.isActive()).thenReturn(false);

        assertFalse(proxyServiceSpyStreams.isSessionRunning("session-abc"));
    }

    @Test
    void whenStreamIsActive_thenProxyIsRunning() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", configMap)).thenReturn(snowStream);
        when(snowStream.isActive()).thenReturn(true);
        proxyService.streamSessionTo("session-abc", out, streamEncoder, configMap);
        when(session.exists("session-abc")).thenReturn(true);

        assertTrue(proxyService.isSessionRunning("session-abc"));
    }

    @Test
    void whenNoActiveStream_thenProxyIsNotRunning() {
        assertFalse(proxyService.isSessionRunning("session-abc"));
    }

    @Test
    void whenStopProxy_thenDeleteSession() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", configMap)).thenReturn(snowStream);
        proxyService.streamSessionTo("session-abc", out, streamEncoder, configMap);
        when(session.exists("session-abc")).thenReturn(true);

        proxyService.stopSession("session-abc");

        verify(session).delete("session-abc");
    }

    @Test
    void whenStopProxy_thenStopStream() throws IOException, InterruptedException, ConsumerThreadException {
        when(snowFactory.create("session-abc", configMap)).thenReturn(snowStream);
        proxyService.streamSessionTo("session-abc", out, streamEncoder, configMap);
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
        when(snowStream.config()).thenReturn(config);
        when(configConverter.toMap(config)).thenReturn(expected);

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

    @Test
    void givenCustomConfiguration_whenStreamToExisting_thenEnsureIsCompatible() throws ConsumerThreadException, IOException, InterruptedException {
        Map<String, String> setup = Map.of("key", "val");
        when(session.exists("session-abc")).thenReturn(true);
        when(streams.get("session-abc")).thenReturn(snowStream);
        PhpSnowConfig snowSetup = mock(PhpSnowConfig.class);
        when(configConverter.fromMap(setup)).thenReturn(snowSetup);

        proxyServiceSpyStreams.streamSessionTo("session-abc", out, streamEncoder, setup);

        verify(snowStream).ensureCompatibleWithConfig("session-abc", snowSetup);
    }

    @Test
    void givenNoCustomConfiguration_whenStreamToExisting_thenNoNeedToEnsureIsCompatible() throws ConsumerThreadException, IOException, InterruptedException {
        when(session.exists("session-abc")).thenReturn(true);
        when(streams.get("session-abc")).thenReturn(snowStream);
        PhpSnowConfig snowSetup = mock(PhpSnowConfig.class);

        proxyServiceSpyStreams.streamSessionTo("session-abc", out, streamEncoder, Collections.emptyMap());

        verify(snowStream, never()).ensureCompatibleWithConfig("session-abc", snowSetup);
    }

}