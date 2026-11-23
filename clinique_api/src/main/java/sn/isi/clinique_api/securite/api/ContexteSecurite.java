package sn.isi.clinique_api.securite.api;

import java.util.Optional;

public interface ContexteSecurite {

    /** L'identifiant du médecin si l'utilisateur connecté est un MEDECIN, sinon vide (pas de restriction). */
    Optional<Long> medecinImpose();
}