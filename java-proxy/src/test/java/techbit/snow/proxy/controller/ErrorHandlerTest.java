package techbit.snow.proxy.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Test
    void givenDeveloperMode_whenErrorWithoutExceptionOccurs_thenNoErrorDetailsReturned() {
        when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(null);

        Map<String, Object> response = new ErrorHandler(true).error(request);

        assertEquals(Map.of(
                "status", false,
                "message", "Server error",
                "exceptionDetails", ""
        ), response);
    }

    @Test
    void givenDeveloperMode_whenErrorWithExceptionOccurs_thenErrorStacktraceIsReturned() {
        when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(new Exception("Stub"));

        Map<String, Object> response = new ErrorHandler(true).error(request);

        assertEquals("Stub", response.get("message"));
        assertTrue(((String)response.get("exceptionDetails")).contains("java.lang.Exception: Stub"));
        assertTrue(((String)response.get("exceptionDetails")).contains("at "));
    }

    @Test
    void givenProductionMode_whenErrorWithExceptionOccurs_thenNoErrorDetailsReturned() {
        when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(new Exception("Stub"));

        Map<String, Object> response = new ErrorHandler(false).error(request);

        assertEquals(Map.of(
                "status", false,
                "message", "Server error",
                "exceptionDetails", ""
        ), response);
    }

}