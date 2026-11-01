package sn.isi.clinique_api.shared;


import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErreurReponse> introuvable(NotFoundException ex) {
        return reponse(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflitException.class)
    public ResponseEntity<ErreurReponse> conflit(ConflitException ex) {
        return reponse(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurReponse> integrite(DataIntegrityViolationException ex) {
        return reponse(HttpStatus.CONFLICT,
                "Opération impossible : des données sont liées à cet élément", List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurReponse> validation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .toList();
        return reponse(HttpStatus.BAD_REQUEST, "Données invalides", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurReponse> corpsIllisible(HttpMessageNotReadableException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "Corps de requête illisible ou mal formé", List.of());
    }

    private ResponseEntity<ErreurReponse> reponse(HttpStatus statut, String message, List<String> details) {
        ErreurReponse corps = new ErreurReponse(statut.value(), message, Instant.now(), details);
        return ResponseEntity.status(statut).body(corps);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErreurReponse> triInvalide(PropertyReferenceException ex) {
        return reponse(HttpStatus.BAD_REQUEST,
                "Critère de tri invalide : " + ex.getPropertyName(), List.of());
    }

    @ExceptionHandler(RequeteInvalideException.class)
    public ResponseEntity<ErreurReponse> requeteInvalide(RequeteInvalideException ex) {
        return reponse(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErreurReponse> accesConcurrent(ObjectOptimisticLockingFailureException ex) {
        return reponse(HttpStatus.CONFLICT,
                "Cet élément vient d'être modifié par quelqu'un d'autre, veuillez réessayer", List.of());
    }
}