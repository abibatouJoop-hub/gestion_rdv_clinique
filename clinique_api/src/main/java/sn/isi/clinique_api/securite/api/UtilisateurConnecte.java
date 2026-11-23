package sn.isi.clinique_api.securite.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import sn.isi.clinique_api.acteurs.api.Role;

import java.security.Principal;

public record UtilisateurConnecte(Long id, String email, Role role, Long medecinId) implements Principal {

    @JsonIgnore
    @Override
    public String getName() {
        return email;
    }
}