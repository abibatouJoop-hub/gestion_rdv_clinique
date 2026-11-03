package sn.isi.clinique_api.catalogue.internal;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import sn.isi.clinique_api.catalogue.api.SpecialiteDto;
import sn.isi.clinique_api.catalogue.api.SpecialiteRequest;

@RestController
@RequestMapping("/api/specialites")
@Tag(name = "Spécialités", description = "Gestion des spécialités médicales")
public class SpecialiteController {

    private final SpecialiteService service;

    public SpecialiteController(SpecialiteService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les spécialités",
            description = "Résultats paginés et triables : page (à partir de 0), size, sort (ex. nom,desc).")
    @ApiResponse(responseCode = "200", description = "Page de spécialités")
    @ApiResponse(responseCode = "400", description = "Critère de tri invalide")
    public PagedModel<SpecialiteDto> lister(
            @ParameterObject @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return new PagedModel<>(service.lister(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une spécialité")
    @ApiResponse(responseCode = "200", description = "Spécialité trouvée")
    @ApiResponse(responseCode = "404", description = "Spécialité introuvable")
    public SpecialiteDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une spécialité")
    @ApiResponse(responseCode = "201", description = "Spécialité créée")
    @ApiResponse(responseCode = "400", description = "Données invalides (nom vide ou trop long)")
    @ApiResponse(responseCode = "409", description = "Une spécialité portant ce nom existe déjà")
    public SpecialiteDto creer(@Valid @RequestBody SpecialiteRequest requete) {
        return service.creer(requete);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une spécialité")
    @ApiResponse(responseCode = "200", description = "Spécialité modifiée")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Spécialité introuvable")
    @ApiResponse(responseCode = "409", description = "Une autre spécialité porte déjà ce nom")
    public SpecialiteDto modifier(@PathVariable Long id, @Valid @RequestBody SpecialiteRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une spécialité")
    @ApiResponse(responseCode = "204", description = "Spécialité supprimée")
    @ApiResponse(responseCode = "404", description = "Spécialité introuvable")
    @ApiResponse(responseCode = "409", description = "Des médecins sont rattachés à cette spécialité")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}