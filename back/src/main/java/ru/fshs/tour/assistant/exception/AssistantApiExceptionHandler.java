package ru.fshs.tour.assistant.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.fshs.tour.controller.dto.error.ErrorResponse;

/**
 * API-level error mapping for assistant module.
 */
@RestControllerAdvice(basePackages = "ru.fshs.tour.assistant")
public class AssistantApiExceptionHandler {

    @ExceptionHandler(UnsupportedAssistantIntentException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedIntent(
            UnsupportedAssistantIntentException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(NotImplementedAssistantFeatureException.class)
    public ResponseEntity<ErrorResponse> handleNotImplemented(
            NotImplementedAssistantFeatureException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_IMPLEMENTED, exception.getMessage(), request);
    }

    @ExceptionHandler(AssistantProcessingException.class)
    public ResponseEntity<ErrorResponse> handleProcessing(
            AssistantProcessingException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request);
    }

    @ExceptionHandler(AssistantException.class)
    public ResponseEntity<ErrorResponse> handleAssistant(
            AssistantException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        ));
    }
}