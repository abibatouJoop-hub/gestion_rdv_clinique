package sn.isi.clinique_api.acteurs.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.isi.clinique_api.acteurs.api.UtilisateurDto;
import sn.isi.clinique_api.acteurs.api.UtilisateurRequest;

import java.security.Principal;

@RestController
@RequestMapping("/api/utilisateurs")
@Tag(name = "Comptes utilisateurs", description = "Gestion des comptes (réservé au rôle ADMIN)")
class UtilisateurController {

    private final UtilisateurGestionService service;

    UtilisateurController(UtilisateurGestionService service) {
        this.service = service;
    }

    @Operation(summary = "Créer un compte (ADMIN, SECRETAIRE ou MEDECIN)")
    @ApiResponse(responseCode = "201", description = "Compte créé")
    @ApiResponse(responseCode = "400", description = "Données invalides ou medecinId incohérent avec le rôle")
    @ApiResponse(responseCode = "404", description = "Médecin introuvable")
    @ApiResponse(responseCode = "409", description = "E-mail déjà utilisé ou médecin déjà doté d'un compte")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UtilisateurDto creer(@Valid @RequestBody UtilisateurRequest requete) {
        return service.creer(requete);
    }

    @Operation(summary = "Lister les comptes")
    @GetMapping
    PagedModel<UtilisateurDto> lister(@ParameterObject Pageable pageable) {
        return new PagedModel<>(service.lister(pageable));
    }

    @Operation(summary = "Désactiver un compte")
    @ApiResponse(responseCode = "409", description = "Impossible de désactiver son propre compte")
    @PatchMapping("/{id}/desactivation")
    UtilisateurDto desactiver(@PathVariable Long id, Principal appelant) {
        return service.desactiver(id, appelant.getName());
    }

    @Operation(summary = "Réactiver un compte")
    @PatchMapping("/{id}/activation")
    UtilisateurDto activer(@PathVariable Long id) {
        return service.activer(id);
    }
}