package techbit.snow.proxy.controller;

import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import techbit.snow.proxy.service.ProxyService;
import techbit.snow.proxy.service.stream.SnowStream;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ProxyControllerTest {

    @Mock
    private ProxyService streaming;

    @Mock
    private OutputStream out;

    private ProxyController controller;


    @BeforeEach
    void setup() {
        controller = new ProxyController(streaming);
    }

    @Test
    void whenIndexRequested_thenThrowException() {
        assertThrows(Exception.class, () -> controller.index());
    }

    @Test
    void whenFaviconRequested_thenDoNothing() {
        ProxyController controller = Mockito.mock(ProxyController.class);
        doCallRealMethod().when(controller).favicon();

        controller.favicon();

        verify(controller).favicon();
        verifyNoMoreInteractions(controller);
    }

    @Test
    void givenNoCustomConfiguration_whenStreamToClient_thenStreamWithEmptyConfigMap() throws IOException, InterruptedException, ExecutionException, ConsumerThreadException {
        StreamingResponseBody responseBody = controller.streamToClient("session-abc", "").get();

        responseBody.writeTo(out);

        verify(streaming).stream("session-abc", out, Collections.emptyMap());
    }

    @Test
    void givenCustomConfiguration_whenStreamToClient_thenStreamWithProperConfigMap() throws IOException, InterruptedException, ExecutionException, ConsumerThreadException {
        StreamingResponseBody responseBody = controller.streamToClient("session-abc", "/key1/value1/key2/value2").get();

        responseBody.writeTo(out);

        verify(streaming).stream("session-abc", out, Map.of(
                "key1", "value1",
                "key2", "value2"
        ));
    }

    @Test
    void givenCustomConfigurationWithMissingValue_whenStreamToClient_thenThrowException() throws IOException, InterruptedException, ExecutionException {
        StreamingResponseBody responseBody = controller.streamToClient("session-abc", "/key1/value1/key2/").get();

        assertThrows(Exception.class, () -> responseBody.writeTo(out));
    }

    @Test
    void givenCustomConfigurationWithEmptyKeyValues_whenStreamToClient_thenThrowException() throws IOException, InterruptedException, ExecutionException {
        StreamingResponseBody responseBody = controller.streamToClient("session-abc", "/key1///value1/").get();

        assertThrows(Exception.class, () -> responseBody.writeTo(out));
    }

    @Test
    void whenStreamDetailsToClient_thenValidDetailsResponded() throws IOException, InterruptedException, ExecutionException {
        Mockito.when(streaming.hasStream("session-abc")).thenReturn(true);
        Mockito.when(streaming.isRunning("session-abc")).thenReturn(true);

        Map<String, Object> response = controller.streamDetails("session-abc");

        verify(streaming).hasStream("session-abc");
        verify(streaming).isRunning("session-abc");

        assertEquals(Map.of(
                "sessionId", "session-abc",
                "exists", true,
                "running", true
        ), response);
    }

    @Test
    void whenStopStreaming_thenStopStream() throws IOException, InterruptedException {
        controller.stopStreaming("session-abc");

        verify(streaming).stopStream("session-abc");
    }

    @Test
    void whenStopStreaming_thenRespondWithInfoMap() throws IOException, InterruptedException {
        Map<String, Object> response = controller.stopStreaming("session-abc");

        assertEquals(Map.of(
                "sessionId", "session-abc",
                "stopped", "ok"
        ), response);
    }


    @Test
    void whenClientAbortDuringStreaming_thenNoErrorOccurs() throws IOException, InterruptedException, ExecutionException, ConsumerThreadException {
        StreamingResponseBody responseBody = controller.streamToClient("session-abc", "").get();

        doThrow(ClientAbortException.class).when(streaming).stream("session-abc", out, Collections.emptyMap());

        assertDoesNotThrow(() -> responseBody.writeTo(out));
    }

    @Test
    void whenThreadInterruptedDuringStreaming_thenErrorOccurs() throws IOException, InterruptedException, ExecutionException, ConsumerThreadException {
        StreamingResponseBody responseBody = controller.streamToClient("session-abc", "").get();

        doThrow(InterruptedException.class).when(streaming).stream("session-abc", out, Collections.emptyMap());

        assertThrows(RuntimeException.class, () -> responseBody.writeTo(out));
    }

}