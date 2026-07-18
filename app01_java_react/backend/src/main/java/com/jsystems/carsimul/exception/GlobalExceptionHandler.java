package com.jsystems.carsimul.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Uniform error responses. Deliberately terse: no stack traces or internal
 * details are ever exposed to the client (see US-1.1 abuse criteria).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    record ApiError(String error, String message) {}

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ApiError> duplicateUsername(DuplicateUsernameException e) {
        return error(HttpStatus.CONFLICT, "DUPLICATE_USERNAME", e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> badCredentials(AuthenticationException e) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid username or password");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(ForbiddenSessionAccessException.class)
    public ResponseEntity<ApiError> forbidden(ForbiddenSessionAccessException e) {
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN_SESSION_ACCESS", e.getMessage());
    }

    @ExceptionHandler(ExamNotActiveException.class)
    public ResponseEntity<ApiError> examNotActive(ExamNotActiveException e) {
        return error(HttpStatus.CONFLICT, "EXAM_ALREADY_TERMINATED", e.getMessage());
    }

    @ExceptionHandler(InvalidTelemetryException.class)
    public ResponseEntity<ApiError> invalidTelemetry(InvalidTelemetryException e) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "IMPLAUSIBLE_TELEMETRY", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> beanValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("validation failed");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", detail);
    }

    /**
     * Malformed JSON, unknown event types, or unexpected extra fields (strict
     * deserialization). Extra fields on exam payloads are a tamper signal.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is malformed or contains unexpected fields");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> illegalArgument(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> fallback(Exception e) {
        log.error("Unhandled exception", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(code, message));
    }
}
