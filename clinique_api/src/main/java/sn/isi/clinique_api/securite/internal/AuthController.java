// AuthController.java
package sn.isi.clinique_api.securite.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.isi.clinique_api.securite.api.ConnexionReponse;
import sn.isi.clinique_api.securite.api.ConnexionRequest;
import sn.isi.clinique_api.securite.api.UtilisateurConnecte;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Connexion et identité de l'utilisateur courant")
class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Se connecter et obtenir un jeton JWT")
    @ApiResponse(responseCode = "200", description = "Connexion réussie")
    @ApiResponse(responseCode = "401", description = "Identifiants invalides")
    @PostMapping("/connexion")
    ConnexionReponse connexion(@Valid @RequestBody ConnexionRequest requete) {
        return authService.connecter(requete);
    }

    @Operation(summary = "Identité de l'utilisateur connecté")
    @ApiResponse(responseCode = "200", description = "Utilisateur courant")
    @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide")
    @GetMapping("/moi")
    UtilisateurConnecte moi(@AuthenticationPrincipal UtilisateurConnecte utilisateur) {
        return utilisateur;
    }
}