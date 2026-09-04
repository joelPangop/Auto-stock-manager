package org.autostock.configs;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.autostock.dtos.ApiError;
import org.autostock.exception.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    /**
     * Contrainte base violee (VIN deja utilise, colonne NOT NULL vide...).
     * Sans ce handler l'erreur remonte en 500 avec le message SQL brut, que le
     * front affiche tel quel : on renvoie un 409 lisible a la place.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> dataIntegrity(DataIntegrityViolationException ex) {
        String cause = ex.getMostSpecificCause().getMessage();
        log.error("Violation de contrainte: {}", cause);
        String message = (cause != null && cause.toLowerCase().contains("vin"))
                ? "Ce VIN est deja utilise par une autre voiture."
                : "Enregistrement impossible : une contrainte d'integrite n'est pas respectee.";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(message));
    }

    /**
     * Corps de requete illisible : le plus souvent une valeur d'enum que le
     * backend ne connait pas. Le message par defaut de Spring est vide cote
     * client, d'ou ce handler.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> notReadable(HttpMessageNotReadableException ex) {
        log.error("Corps de requete invalide: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiError("Donnees envoyees invalides : " + ex.getMostSpecificCause().getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> runtimeException(RuntimeException ex) {
        log.error("Erreur interne: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(ex.getMessage()));
    }
}
