package zw.codinho.ridehail.shared.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zw.codinho.ridehail.shared.api.ApiResponse;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Map<String, Object>>> handleBusinessException(BusinessException exception) {
        return response(exception.getStatus(), exception.getMessage(), Map.of("error", exception.getClass().getSimpleName()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Map<String, Object>>> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (left, right) -> left));

        return response(HttpStatus.BAD_REQUEST, "Validation failed", Map.of("fields", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiResponse<Map<String, Object>>> handleConstraintViolation(ConstraintViolationException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of("error", "ConstraintViolation"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Map<String, Object>>> handleAccessDenied(AccessDeniedException exception) {
        return response(HttpStatus.FORBIDDEN, "Access Denied", Map.of("error", "AccessDenied"));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiResponse<Map<String, Object>>> handleAuthenticationException(AuthenticationException exception) {
        return response(HttpStatus.UNAUTHORIZED, "Authentication required", Map.of("error", "Unauthorized"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Map<String, Object>>> handleUnhandledException(Exception exception) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), Map.of("error", "UnexpectedError"));
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> response(HttpStatus status, String message, Map<String, Object> details) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, message, details, OffsetDateTime.now()));
    }
}
