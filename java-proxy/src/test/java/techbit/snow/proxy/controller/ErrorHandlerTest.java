package techbit.snow.proxy.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @Mock
    private HttpServletRequest request;

    private ErrorHandler errorHandler;

    @BeforeEach
    void setup() {
        errorHandler = new ErrorHandler();
    }

    @Test
    void whenErrorWithoutExceptionOccurs_thenNoErrorDetailsReturned() {
        Mockito.when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(null);

        Map<String, Object> response = errorHandler.error(request);

        assertEquals(Map.of(
                "status", false,
                "message", "Server error",
                "exceptionDetails", ""
        ), response);
    }

    @Test
    void whenErrorWithExceptionOccurs_thenErrorDetailsReturned() {
        try {
            throw new Exception("Stub");
        } catch(Exception e) {
            Mockito.when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(e);
        }

        Map<String, Object> response = errorHandler.error(request);

        assertEquals("Stub", response.get("message"));
        Assertions.assertTrue(((String)response.get("exceptionDetails")).contains("java.lang.Exception: Stub"));
        Assertions.assertTrue(((String)response.get("exceptionDetails")).contains("at "));
    }

}