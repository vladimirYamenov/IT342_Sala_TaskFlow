package edu.cit.sala.TaskFlow.shared.config;

import edu.cit.sala.TaskFlow.shared.error.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        String errorCode = mapStatusToCode(ex.getStatusCode().value(), ex.getReason());

        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .message(ex.getReason())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .errorCode("VALID-001")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Request parsing error: {}", ex.getMessage());
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .errorCode("VALID-001")
                .message("Invalid request format: " + ex.getMostSpecificCause().getMessage())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .errorCode("SYSTEM-001")
                .message("Internal server error")
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String mapStatusToCode(int status, String reason) {
        if (status == 401) return "AUTH-001";
        if (status == 403) return "AUTH-003";
        if (status == 404) return "DB-001";
        if (status == 409) return "VALID-002";
        if (status == 400) return "VALID-001";
        return "SYSTEM-001";
    }
}
