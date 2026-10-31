package sn.isi.clinique_api.catalogue.api;

public record MedecinDto(Long id, String nom, String prenom, Long specialiteId, String specialiteNom) {
}