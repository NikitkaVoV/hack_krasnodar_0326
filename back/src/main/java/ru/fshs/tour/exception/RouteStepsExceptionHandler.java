package ru.fshs.tour.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import ru.fshs.tour.controller.dto.error.SimpleErrorDto;
import ru.fshs.tour.controller.impl.RouteStepsController;

@RestControllerAdvice(assignableTypes = RouteStepsController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RouteStepsExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<SimpleErrorDto> handleValidation(Exception exception) {
        var message = exception instanceof MissingServletRequestParameterException
                ? ((MissingServletRequestParameterException) exception).getParameterName() + " is required"
                : exception.getMessage();
        return ResponseEntity.unprocessableEntity()
                .body(new SimpleErrorDto(message, "VALIDATION_ERROR"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<SimpleErrorDto> handleResponseStatus(ResponseStatusException exception) {
        if (exception.getStatusCode().value() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new SimpleErrorDto("Route not found", "ROUTE_NOT_FOUND"));
        }
        return ResponseEntity.status(exception.getStatusCode())
                .body(new SimpleErrorDto(exception.getReason() != null ? exception.getReason() : "Unexpected error",
                        "INTERNAL_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorDto> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleErrorDto("Internal server error", "INTERNAL_ERROR"));
    }
}
