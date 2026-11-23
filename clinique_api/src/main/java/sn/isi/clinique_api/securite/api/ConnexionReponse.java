// ConnexionReponse.java
package sn.isi.clinique_api.securite.api;

import sn.isi.clinique_api.acteurs.api.Role;

public record ConnexionReponse(String token, String type, long expireDansSecondes,
                               String email, Role role, Long medecinId) {
}