package ru.fshs.tour.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.fshs.tour.controller.dto.error.SimpleErrorDto;
import ru.fshs.tour.controller.impl.MeRoutesController;

@RestControllerAdvice(assignableTypes = MeRoutesController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MeRoutesExceptionHandler {

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<SimpleErrorDto> handleNotFound(RouteNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorDto(exception.getMessage(), "ROUTE_NOT_FOUND"));
    }

    @ExceptionHandler(RouteForbiddenException.class)
    public ResponseEntity<SimpleErrorDto> handleForbidden(RouteForbiddenException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new SimpleErrorDto(exception.getMessage(), "ROUTE_FORBIDDEN"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<SimpleErrorDto> handleUnauthorized(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new SimpleErrorDto("Требуется авторизация", "UNAUTHORIZED"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<SimpleErrorDto> handleInvalidId(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SimpleErrorDto("Неверный формат идентификатора маршрута", "INVALID_ROUTE_ID"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorDto> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleErrorDto("Внутренняя ошибка сервера", "INTERNAL_ERROR"));
    }
}
