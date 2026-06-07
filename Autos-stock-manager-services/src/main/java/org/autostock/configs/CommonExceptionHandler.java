package org.autostock.configs;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.autostock.dtos.ApiError;
import org.autostock.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class CommonExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Void> notFoundException(NotFoundException ex) {
        log.error(ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> entityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> illegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> illegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiError> unsupportedOperation(UnsupportedOperationException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> runtimeException(RuntimeException ex) {
        log.error("Erreur interne: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(ex.getMessage()));
    }
}
