package sn.isi.clinique_api.acteurs.api;

import java.util.Optional;

public interface UtilisateurApi {

    /** Retrouve un compte actif par e-mail (pour la connexion). */
    Optional<UtilisateurAuthDto> trouverActifParEmail(String email);
}