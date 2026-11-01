package sn.isi.clinique_api.acteurs.api;

public record PatientDto(Long id, String nom, String prenom, String email,
                         String telephone, int pointsPenalite, boolean bloque) {
}