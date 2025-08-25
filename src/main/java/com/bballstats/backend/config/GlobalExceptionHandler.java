package com.bballstats.backend.config;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "NOT_FOUND",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "CONFLICT",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity.badRequest().body(Map.of(
                "error", "VALIDATION_ERROR",
                "fields", fieldErrors
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraint(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "VALIDATION_ERROR",
                "message", ex.getMessage()
        ));
    }

    // 401 kad su kredencijali loši
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleBadCredentials(Exception ex) {
        return Map.of(
                "error", "UNAUTHORIZED",
                "message", "Bad credentials"
        );
    }

    // (opciono) 401 i za slučaj da username/email ne postoji
    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleUserNotFound(Exception ex) {
        return Map.of(
                "error", "UNAUTHORIZED",
                "message", "Bad credentials"
        );
    }

    // ✅ JEDAN handler za DataIntegrityViolationException (nema duplikata)
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDataIntegrity(DataIntegrityViolationException ex) {
        // uzmi "root cause" poruku iz baze (MySQL)
        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);
        String raw = (root != null && root.getMessage() != null) ? root.getMessage() : ex.getMessage();
        String msg = raw != null ? raw.toLowerCase() : "";

        String message;
        // prepoznaj FK slučaj (npr. fk_boxscore_player, box_scores, foreign key, references…)
        if (msg.contains("foreign key") || msg.contains("references")
                || msg.contains("fk_boxscore_player") || msg.contains("box_scores")) {
            message = "Player cannot be deleted because he has related box scores.";
        } else if (msg.contains("duplicate") || msg.contains("unique")) {
            message = "Duplicate value (constraint violation).";
        } else {
            message = "Operation not allowed due to related records (constraints).";
        }
        return Map.of("message", message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", ex.getMessage()
        ));
    }
}
