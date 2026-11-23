package sn.isi.clinique_api.acteurs.api;

public record UtilisateurDto(Long id, String email, Role role, Long medecinId, boolean actif) {
}