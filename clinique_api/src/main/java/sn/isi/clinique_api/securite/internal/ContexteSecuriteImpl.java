package sn.isi.clinique_api.securite.internal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import sn.isi.clinique_api.acteurs.api.Role;
import sn.isi.clinique_api.securite.api.ContexteSecurite;
import sn.isi.clinique_api.securite.api.UtilisateurConnecte;

import java.util.Optional;

@Component
class ContexteSecuriteImpl implements ContexteSecurite {

    @Override
    public Optional<Long> medecinImpose() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UtilisateurConnecte u && u.role() == Role.MEDECIN) {
            return Optional.of(u.medecinId());
        }
        return Optional.empty();
    }
}