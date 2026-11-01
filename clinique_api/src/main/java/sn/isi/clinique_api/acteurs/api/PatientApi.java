package sn.isi.clinique_api.acteurs.api;

public interface PatientApi {

    /** Nombre de points de pénalité à partir duquel un patient est bloqué. */
    int SEUIL_BLOCAGE = 5;

    /** Retourne le patient, ou lève une erreur 404 s'il n'existe pas. */
    PatientDto trouver(Long id);

    /** Ajoute des points de pénalité au patient (nombre strictement positif). */
    void ajouterPenalite(Long id, int points);
}