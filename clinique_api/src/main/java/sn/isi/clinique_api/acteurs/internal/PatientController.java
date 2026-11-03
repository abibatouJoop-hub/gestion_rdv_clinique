package sn.isi.clinique_api.acteurs.internal;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import sn.isi.clinique_api.acteurs.api.PatientDto;
import sn.isi.clinique_api.acteurs.api.PatientRequest;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "Gestion des patients de la clinique")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Rechercher des patients",
            description = "Recherche sur le nom, le prénom ou l'e-mail. Résultats paginés et triables. "
                    + "Le champ « bloque » vaut true à partir de 5 points de pénalité.")
    @ApiResponse(responseCode = "200", description = "Page de patients")
    @ApiResponse(responseCode = "400", description = "Critère de tri invalide")
    public PagedModel<PatientDto> rechercher(
            @Parameter(description = "Fragment du nom, du prénom ou de l'e-mail", example = "ndi")
            @RequestParam(required = false) String recherche,
            @ParameterObject @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return new PagedModel<>(service.rechercher(recherche, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un patient")
    @ApiResponse(responseCode = "200", description = "Patient trouvé")
    @ApiResponse(responseCode = "404", description = "Patient introuvable")
    public PatientDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un patient")
    @ApiResponse(responseCode = "201", description = "Patient créé, avec 0 point de pénalité")
    @ApiResponse(responseCode = "400", description = "Données invalides (champ vide, e-mail mal formé)")
    @ApiResponse(responseCode = "409", description = "Un patient avec cet e-mail existe déjà")
    public PatientDto creer(@Valid @RequestBody PatientRequest requete) {
        return service.creer(requete);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un patient",
            description = "Les points de pénalité ne sont pas modifiables ici : ils dépendent des rendez-vous.")
    @ApiResponse(responseCode = "200", description = "Patient modifié")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Patient introuvable")
    @ApiResponse(responseCode = "409", description = "Un autre patient utilise déjà cet e-mail")
    public PatientDto modifier(@PathVariable Long id, @Valid @RequestBody PatientRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un patient")
    @ApiResponse(responseCode = "204", description = "Patient supprimé")
    @ApiResponse(responseCode = "404", description = "Patient introuvable")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}