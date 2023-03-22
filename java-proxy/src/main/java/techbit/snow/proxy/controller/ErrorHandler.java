package techbit.snow.proxy.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@Log4j2
@RestController
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ErrorHandler implements ErrorController {

    private final boolean canShowStacktrace;

    public ErrorHandler(
            @Autowired @Qualifier("is.developer.mode") boolean canShowStacktrace
    ) {
        this.canShowStacktrace = canShowStacktrace;
    }

    @RequestMapping("/error")
    public Map<String, Object> error(HttpServletRequest request) {
        String message = "Server error";
        String exceptionDetails = "";

        Exception exception = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (canShowStacktrace && exception != null) {
            message = exception.getMessage();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            exceptionDetails = exception + "\n\n" + sw;
        }

        log.error(message, exception);

        return Map.of(
                "status", false,
                "message", message,
                "exceptionDetails", exceptionDetails
        );
    }
}
