package techbit.snow.proxy.controller;

import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import techbit.snow.proxy.service.ProxyService;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;

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
        assertThrows(IllegalArgumentException.class, controller::index);
    }

    @Test
    void whenFaviconRequested_thenDoNothing() {
        ProxyController controller = mock(ProxyController.class);
        doCallRealMethod().when(controller).favicon();

        controller.favicon();

        verify(controller).favicon();
        verifyNoMoreInteractions(controller);
    }

    @Test
    void givenNoCustomConfiguration_whenStreamToClient_thenStreamWithEmptyConfigMap() throws IOException, InterruptedException, ExecutionException, ConsumerThreadException {
        controller.streamToClient("session-abc", "").get().writeTo(out);

        verify(streaming).start("session-abc", out, Collections.emptyMap());
    }

    @Test
    void givenCustomConfiguration_whenStreamToClient_thenStreamWithProperConfigMap() throws IOException, InterruptedException, ExecutionException, ConsumerThreadException {
        controller.streamToClient("session-abc", "/key1/value1/key2/value2").get().writeTo(out);

        verify(streaming).start("session-abc", out, Map.of(
                "key1", "value1",
                "key2", "value2"
        ));
    }

    @Test
    void givenCustomConfigurationWithMissingValue_whenStreamToClient_thenThrowException() throws InterruptedException, ExecutionException {
        StreamingResponseBody responseBody = controller.streamToClient(
                "session-abc", "/key1/value1/key2/").get();

        assertThrows(IllegalArgumentException.class, () -> responseBody.writeTo(out));
    }

    @Test
    void givenCustomConfigurationWithEmptyKeyValues_whenStreamToClient_thenThrowException() throws InterruptedException, ExecutionException {
        StreamingResponseBody responseBody = controller.streamToClient(
                "session-abc", "/key1///value1/").get();

        assertThrows(IllegalArgumentException.class, () -> responseBody.writeTo(out));
    }

    @Test
    void whenStreamDetails_thenValidDetailsResponded() {
        when(streaming.hasStream("session-abc")).thenReturn(true);
        when(streaming.isRunning("session-abc")).thenReturn(true);

        Map<String, Object> response = controller.streamDetails("session-abc");

        assertEquals(Map.of(
                "sessionId", "session-abc",
                "exists", true,
                "running", true
        ), response);
    }

    @Test
    void whenStopStreamingRequested_thenStopStream() throws IOException, InterruptedException {
        controller.stopStreaming("session-abc");

        verify(streaming).stop("session-abc");
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
        doThrow(ClientAbortException.class).when(streaming).start("session-abc", out, Collections.emptyMap());

        assertDoesNotThrow(() -> controller.streamToClient("session-abc", "").get().writeTo(out));
    }

    @Test
    void whenThreadInterruptedDuringStreaming_thenErrorOccurs() throws IOException, InterruptedException, ConsumerThreadException {
        doThrow(InterruptedException.class).when(streaming).start("session-abc", out, Collections.emptyMap());

        assertThrows(IOException.class, () -> controller.streamToClient("session-abc", "").get().writeTo(out));
    }

}