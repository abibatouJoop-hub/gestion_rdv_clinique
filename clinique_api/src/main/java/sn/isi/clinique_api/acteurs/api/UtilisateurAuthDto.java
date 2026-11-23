package sn.isi.clinique_api.acteurs.api;

public record UtilisateurAuthDto(Long id, String email, String motDePasseHache, Role role, Long medecinId) {
}