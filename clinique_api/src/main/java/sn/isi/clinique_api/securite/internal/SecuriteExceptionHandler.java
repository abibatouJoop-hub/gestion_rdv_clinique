// SecuriteExceptionHandler.java
package sn.isi.clinique_api.securite.internal;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class SecuriteExceptionHandler {

    @ExceptionHandler(IdentifiantsInvalidesException.class)
    ResponseEntity<Map<String, Object>> identifiantsInvalides(IdentifiantsInvalidesException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ReponsesSecurite.corps("IDENTIFIANTS_INVALIDES", e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> accesRefuse(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ReponsesSecurite.corps("ACCES_REFUSE", "Vous n'avez pas le droit d'effectuer cette action"));
    }
}