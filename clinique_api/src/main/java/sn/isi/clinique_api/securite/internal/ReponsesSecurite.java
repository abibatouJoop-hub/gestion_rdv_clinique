// ReponsesSecurite.java : réponses 401/403 au même format que ErreurReponse
package sn.isi.clinique_api.securite.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
class ReponsesSecurite {

    private final ObjectMapper mapper;

    ReponsesSecurite(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    static Map<String, Object> corps(String code, String message) {
        return Map.of("code", code, "message", message,
                "horodatage", LocalDateTime.now().toString(), "details", List.of());
    }

    void nonAuthentifie(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        ecrire(res, 401, corps("NON_AUTHENTIFIE", "Authentification requise ou jeton invalide"));
    }

    void interdit(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        ecrire(res, 403, corps("ACCES_REFUSE", "Vous n'avez pas le droit d'effectuer cette action"));
    }

    private void ecrire(HttpServletResponse res, int statut, Map<String, Object> corps) throws IOException {
        res.setStatus(statut);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getOutputStream(), corps);
    }
}