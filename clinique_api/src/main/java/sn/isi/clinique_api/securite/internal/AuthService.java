// AuthService.java
package sn.isi.clinique_api.securite.internal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.isi.clinique_api.acteurs.api.UtilisateurApi;
import sn.isi.clinique_api.acteurs.api.UtilisateurAuthDto;
import sn.isi.clinique_api.securite.api.ConnexionReponse;
import sn.isi.clinique_api.securite.api.ConnexionRequest;

@Service
class AuthService {

    private final UtilisateurApi utilisateurs;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    AuthService(UtilisateurApi utilisateurs, PasswordEncoder encoder, JwtService jwtService) {
        this.utilisateurs = utilisateurs;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    ConnexionReponse connecter(ConnexionRequest requete) {
        UtilisateurAuthDto u = utilisateurs.trouverActifParEmail(requete.email())
                .filter(c -> encoder.matches(requete.motDePasse(), c.motDePasseHache()))
                .orElseThrow(IdentifiantsInvalidesException::new); // même message : on ne révèle pas si l'e-mail existe
        return new ConnexionReponse(jwtService.generer(u), "Bearer", jwtService.dureeEnSecondes(),
                u.email(), u.role(), u.medecinId());
    }
}