package com.sitandi.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler
 */
@RestControllerAdvice(basePackages = "com.sitandi.controller.api")
public class GlobalExceptionHandler {

    private Map<String, Object> baseBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(baseBody(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validasi input gagal");
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        // Validation Errors
        ex.getBindingResult().getGlobalErrors()
                .forEach(err -> fieldErrors.put(err.getObjectName(), err.getDefaultMessage()));
        body.put("errors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<Object> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(baseBody(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(baseBody(HttpStatus.FORBIDDEN, "Anda tidak memiliki akses untuk operasi ini"));
    }

    // Inactive Account
    @ExceptionHandler(AkunBelumAktifException.class)
    public ResponseEntity<Object> handleAkunBelumAktif(AkunBelumAktifException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(baseBody(HttpStatus.FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler(TndeLoginException.class)
    public ResponseEntity<Object> handleTndeLogin(TndeLoginException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(baseBody(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    // TNDE Server Error
    @ExceptionHandler(TndeServerException.class)
    public ResponseEntity<Object> handleTndeServer(TndeServerException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(baseBody(HttpStatus.BAD_GATEWAY, ex.getMessage()));
    }

    // Authentication Error
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(baseBody(HttpStatus.UNAUTHORIZED, "Username atau password salah"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        // Generic Error
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(baseBody(HttpStatus.INTERNAL_SERVER_ERROR, "Terjadi kesalahan pada server"));
    }
}

