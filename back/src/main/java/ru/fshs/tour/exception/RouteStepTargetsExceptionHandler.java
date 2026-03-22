package ru.fshs.tour.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.fshs.tour.controller.dto.route_steps.RouteStepTargetErrorDto;
import ru.fshs.tour.controller.impl.RouteStepTargetsController;

@RestControllerAdvice(assignableTypes = RouteStepTargetsController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RouteStepTargetsExceptionHandler {

    @ExceptionHandler(InvalidIdFormatException.class)
    public ResponseEntity<RouteStepTargetErrorDto> handleInvalidId(InvalidIdFormatException exception) {
        return ResponseEntity.unprocessableEntity().body(error("INVALID_ID", "Invalid id format"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<RouteStepTargetErrorDto> handleResponseStatus(ResponseStatusException exception) {
        if (exception.getStatusCode().value() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("NOT_FOUND", "Entity not found"));
        }
        return ResponseEntity.status(exception.getStatusCode()).body(error("INTERNAL_ERROR", "Internal server error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RouteStepTargetErrorDto> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("INTERNAL_ERROR", "Internal server error"));
    }

    private RouteStepTargetErrorDto error(String code, String message) {
        return new RouteStepTargetErrorDto(
                message,
                code,
                new RouteStepTargetErrorDto.ErrorBody(code, message, null)
        );
    }
}
