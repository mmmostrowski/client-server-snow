package techbit.snow.proxy.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.exception.UserException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Test
    void givenDeveloperMode_whenErrorWithNoExceptionOccurs_thenNoErrorDetailsReturned() {
        when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(null);

        Map<String, Object> response = new ErrorHandler(true).error(request);

        assertEquals(Map.of(
                "status", false,
                "message", "Server error",
                "exceptionDetails", ""
        ), response);
    }

    @Test
    void givenDeveloperMode_whenErrorWithExceptionOccurs_thenStacktraceDetailsAreReturned() {
        when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(new Exception("Stub"));

        Map<String, Object> response = new ErrorHandler(true).error(request);

        assertEquals("Stub", response.get("message"));
        assertTrue(((String)response.get("exceptionDetails")).contains("java.lang.Exception: Stub"));
        assertTrue(((String)response.get("exceptionDetails")).contains("at "));
    }

    @Test
    void givenProductionMode_whenExceptionOccurs_thenNoErrorDetailsReturned() {
        when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(new Exception("Stub"));

        Map<String, Object> response = new ErrorHandler(false).error(request);

        assertEquals(Map.of(
                "status", false,
                "message", "Server error",
                "exceptionDetails", ""
        ), response);
    }

    @Test
    void givenProductionMode_whenUserExceptionOccurs_thenItIsTransmittedToUser() {
        when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(
                new Exception(new UserException("Stub") {}));

        Map<String, Object> response = new ErrorHandler(false).error(request);

        assertEquals(Map.of(
                "status", false,
                "message", "Stub",
                "exceptionDetails", ""
        ), response);
    }

    @Test
    void givenProductionMode_whenConstraintViolationExceptionOccurs_thenItIsTransmittedToUser() {
        Exception exception = Mockito.mock(ConstraintViolationException.class);
        when(exception.getMessage()).thenReturn("Stub");
        Exception wrappingException = new Exception(exception);
        when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(wrappingException);

        Map<String, Object> response = new ErrorHandler(false).error(request);

        assertEquals(Map.of(
                "status", false,
                "message", "Stub",
                "exceptionDetails", ""
        ), response);
    }


}