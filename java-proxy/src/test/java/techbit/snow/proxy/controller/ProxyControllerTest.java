package techbit.snow.proxy.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import techbit.snow.proxy.exception.InvalidRequestException;
import techbit.snow.proxy.service.ProxyServiceImpl;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;
import techbit.snow.proxy.service.stream.encoding.PlainTextStreamEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProxyControllerTest {

    @Mock
    private ProxyServiceImpl streaming;

    @Mock
    private OutputStream out;

    @Mock
    private PlainTextStreamEncoder textStreamEncoder;
    @Mock
    private HttpServletRequest request;
    private ProxyController controller;


    @BeforeEach
    void setup() {
        lenient().when(request.getScheme()).thenReturn("http");
        lenient().when(request.getServerName()).thenReturn("domain.com");
        lenient().when(request.getServerPort()).thenReturn(1234);

        controller = new ProxyController(streaming, textStreamEncoder);
    }


    @Test
    void whenNotEnoughParamsRequested_thenThrowException() {
        assertThrows(InvalidRequestException.class, controller::insufficientParams);
    }

    @Test
    void givenNoCustomConfiguration_whenStartSession_thenStreamWithEmptyConfigMap() throws IOException {
        Map<?, ?> details = controller.startSession("session-abc", "", request);

        verify(streaming).startSession("session-abc", Collections.emptyMap());
        assertFalse(details.isEmpty());
    }

    @Test
    void givenNoCustomConfiguration_whenStartSession_thenProvideDetails() throws IOException {
        when(streaming.hasSession("session-abc")).thenReturn(true);
        when(streaming.isSessionRunning("session-abc")).thenReturn(true);

        Map<?, ?> details = controller.startSession(
                "session-abc", "", request);

        assertFalse(details.isEmpty());
        assertExpectedDetails(details);
    }

    @Test
    void whenAskingForDetails_thenProvideDetails() {
        when(streaming.hasSession("session-abc")).thenReturn(true);
        when(streaming.isSessionRunning("session-abc")).thenReturn(true);

        Map<?,?> details = controller.streamDetails(
                "session-abc", request);

        assertExpectedDetails(details);
    }

    private void assertExpectedDetails(Map<?, ?> details) {
        assertEquals(Map.of(
                "exists", true,
                "running", true,
                "sessionId", "session-abc",
                "streamTextUrl", "http://domain.com:1234/text/session-abc",
                "streamWebsocketsStompBrokerUrl", "http://domain.com:1234/ws/",
                "streamWebsocketsUrl", "/app/stream/session-abc"
        ), details);
    }

    @Test
    void givenNoCustomConfiguration_whenStreamTextToClient_thenStreamWithEmptyConfigMap() throws IOException, InterruptedException, ExecutionException, ConsumerThreadException {
        controller.streamTextToClient("session-abc", "").get().writeTo(out);

        verify(streaming).streamSessionTo("session-abc", out, textStreamEncoder, Collections.emptyMap());
    }

    @Test
    void givenCustomConfiguration_whenStreamTextToClient_thenStreamWithProperConfigMap() throws IOException, InterruptedException, ExecutionException, ConsumerThreadException {
        controller.streamTextToClient("session-abc", "/key1/value1/key2/value2").get().writeTo(out);

        verify(streaming).streamSessionTo("session-abc", out, textStreamEncoder, Map.of(
                "key1", "value1",
                "key2", "value2"
        ));
    }

    @Test
    void givenCustomConfigurationWithMissingValue_whenStreamToClient_thenThrowException() throws InterruptedException, ExecutionException {
        StreamingResponseBody responseBody = controller.streamTextToClient(
                "session-abc", "/key1/value1/key2/").get();

        assertThrows(InvalidRequestException.class, () -> responseBody.writeTo(out));
    }

    @Test
    void givenCustomConfigurationWithEmptyKeyValues_whenStreamToClient_thenThrowException() throws InterruptedException, ExecutionException {
        StreamingResponseBody responseBody = controller.streamTextToClient(
                "session-abc", "/key1///value1/").get();

        assertThrows(InvalidRequestException.class, () -> responseBody.writeTo(out));
    }

    @Test
    void whenStreamDetails_thenValidDetailsResponded() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(streaming.hasSession("session-abc")).thenReturn(true);
        when(streaming.isSessionRunning("session-abc")).thenReturn(true);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("domain.com");
        when(request.getServerPort()).thenReturn(1234);

        Map<String, Object> response = controller.streamDetails("session-abc", request);

        assertEquals(Map.of(
                "sessionId", "session-abc",
                "exists", true,
                "running", true,
                "streamTextUrl", "http://domain.com:1234/text/session-abc",
                "streamWebsocketsStompBrokerUrl", "http://domain.com:1234/ws/",
                "streamWebsocketsUrl", "/app/stream/session-abc"
        ), response);
    }

    @Test
    void whenStopStreamingRequested_thenStopStream() throws IOException, InterruptedException {
        controller.stopStreaming("session-abc");

        verify(streaming).stopSession("session-abc");
    }

    @Test
    void whenStopStreamingRequested_thenRespondWithInfoMap() throws IOException, InterruptedException {
        Map<String, Object> response = controller.stopStreaming("session-abc");

        assertEquals(Map.of(
                "sessionId", "session-abc",
                "stopped", "ok"
        ), response);
    }

    @Test
    void whenClientAbortDuringStreaming_thenNoErrorOccurs() throws IOException, InterruptedException, ConsumerThreadException {
        doThrow(ClientAbortException.class).when(streaming).streamSessionTo(
                "session-abc", out, textStreamEncoder, Collections.emptyMap());

        assertDoesNotThrow(() -> controller.streamTextToClient("session-abc", "").get().writeTo(out));
    }

    @Test
    void whenThreadInterruptedDuringStreaming_thenErrorOccurs() throws IOException, InterruptedException, ConsumerThreadException {
        doThrow(InterruptedException.class).when(streaming).streamSessionTo(
                "session-abc", out, textStreamEncoder, Collections.emptyMap());

        assertThrows(IOException.class, () -> controller.streamTextToClient("session-abc", "").get().writeTo(out));
    }

}