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
import sn.isi.clinique_api.catalogue.api.CreneauDto;
import sn.isi.clinique_api.catalogue.api.CreneauRequest;

@RestController
@RequestMapping("/api/creneaux")
@Tag(name = "Créneaux", description = "Gestion des créneaux de consultation des médecins")
public class CreneauController {

    private final CreneauService service;

    public CreneauController(CreneauService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les créneaux",
            description = "Filtres facultatifs : médecin, jour, disponibilité. Résultats paginés et triables.")
    @ApiResponse(responseCode = "200", description = "Page de créneaux")
    @ApiResponse(responseCode = "400", description = "Paramètre invalide (date mal formée, tri inconnu)")
    public PagedModel<CreneauDto> lister(
            @Parameter(description = "Identifiant du médecin", example = "1")
            @RequestParam(required = false) Long medecinId,
            @Parameter(description = "Jour recherché (AAAA-MM-JJ)", example = "2026-11-16")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "true : seulement les créneaux libres, false : seulement les réservés")
            @RequestParam(required = false) Boolean libre,
            @ParameterObject @PageableDefault(size = 20, sort = "debut") Pageable pageable) {
        return new PagedModel<>(service.lister(medecinId, date, libre, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un créneau")
    @ApiResponse(responseCode = "200", description = "Créneau trouvé")
    @ApiResponse(responseCode = "404", description = "Créneau introuvable")
    public CreneauDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un créneau",
            description = "Le créneau doit commencer dans le futur, finir après son début, "
                    + "et ne pas chevaucher un autre créneau du même médecin.")
    @ApiResponse(responseCode = "201", description = "Créneau créé")
    @ApiResponse(responseCode = "400", description = "Données invalides (fin avant début, date passée)")
    @ApiResponse(responseCode = "404", description = "Médecin introuvable")
    @ApiResponse(responseCode = "409", description = "Chevauche un autre créneau du même médecin")
    public CreneauDto creer(@Valid @RequestBody CreneauRequest requete) {
        return service.creer(requete);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un créneau", description = "Impossible si le créneau est déjà réservé.")
    @ApiResponse(responseCode = "200", description = "Créneau modifié")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Créneau ou médecin introuvable")
    @ApiResponse(responseCode = "409", description = "Créneau réservé ou chevauchement")
    public CreneauDto modifier(@PathVariable Long id, @Valid @RequestBody CreneauRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un créneau", description = "Impossible si le créneau est déjà réservé.")
    @ApiResponse(responseCode = "204", description = "Créneau supprimé")
    @ApiResponse(responseCode = "404", description = "Créneau introuvable")
    @ApiResponse(responseCode = "409", description = "Le créneau est déjà réservé")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}