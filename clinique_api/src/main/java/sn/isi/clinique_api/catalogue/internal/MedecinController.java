package sn.isi.clinique_api.catalogue.internal;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
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
import sn.isi.clinique_api.catalogue.api.MedecinDto;
import sn.isi.clinique_api.catalogue.api.MedecinRequest;

@RestController
@RequestMapping("/api/medecins")
@Tag(name = "Médecins", description = "Gestion et recherche des médecins")
public class MedecinController {

    private final MedecinService service;

    public MedecinController(MedecinService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Rechercher des médecins",
            description = "Recherche multicritère : tous les filtres sont facultatifs et se combinent. "
                    + "Résultats paginés et triables.")
    @ApiResponse(responseCode = "200", description = "Page de médecins correspondant aux critères")
    @ApiResponse(responseCode = "400", description = "Paramètre invalide (date mal formée, tri inconnu)")
    public PagedModel<MedecinDto> rechercher(
            @Parameter(description = "Fragment du nom ou du prénom, sans tenir compte des majuscules",
                    example = "dia")
            @RequestParam(required = false) String nom,
            @Parameter(description = "Identifiant de la spécialité", example = "1")
            @RequestParam(required = false) Long specialiteId,
            @Parameter(description = "Médecins ayant au moins un créneau ce jour-là (AAAA-MM-JJ)",
                    example = "2026-11-16")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "true : seulement les médecins ayant un créneau libre à venir")
            @RequestParam(required = false) Boolean disponible,
            @ParameterObject @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return new PagedModel<>(service.rechercher(nom, specialiteId, date, disponible, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un médecin")
    @ApiResponse(responseCode = "200", description = "Médecin trouvé")
    @ApiResponse(responseCode = "404", description = "Médecin introuvable")
    public MedecinDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un médecin")
    @ApiResponse(responseCode = "201", description = "Médecin créé")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Spécialité introuvable")
    @ApiResponse(responseCode = "409", description = "Conflit avec une donnée existante")
    public MedecinDto creer(@Valid @RequestBody MedecinRequest requete) {
        return service.creer(requete);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un médecin")
    @ApiResponse(responseCode = "200", description = "Médecin modifié")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Médecin ou spécialité introuvable")
    @ApiResponse(responseCode = "409", description = "Conflit avec une donnée existante")
    public MedecinDto modifier(@PathVariable Long id, @Valid @RequestBody MedecinRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un médecin")
    @ApiResponse(responseCode = "204", description = "Médecin supprimé")
    @ApiResponse(responseCode = "404", description = "Médecin introuvable")
    @ApiResponse(responseCode = "409", description = "Le médecin a encore des créneaux")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}