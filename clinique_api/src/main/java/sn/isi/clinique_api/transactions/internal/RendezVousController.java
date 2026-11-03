package sn.isi.clinique_api.transactions.internal;

import java.util.Map;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import sn.isi.clinique_api.transactions.api.AttenteDto;
import sn.isi.clinique_api.transactions.api.RendezVousDto;
import sn.isi.clinique_api.transactions.api.RendezVousRequest;
import sn.isi.clinique_api.transactions.api.StatutRendezVous;

@RestController
@RequestMapping("/api/rendez-vous")
@Tag(name = "Rendez-vous", description = "Prise de rendez-vous, annulation, absences, liste d'attente et rappels")
public class RendezVousController {

    private final RendezVousService service;

    public RendezVousController(RendezVousService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les rendez-vous",
            description = "Filtres facultatifs : patient et statut. Résultats paginés et triables.")
    @ApiResponse(responseCode = "200", description = "Page de rendez-vous")
    @ApiResponse(responseCode = "400", description = "Paramètre invalide (statut inconnu, tri inconnu)")
    public PagedModel<RendezVousDto> lister(
            @Parameter(description = "Identifiant du patient", example = "1")
            @RequestParam(required = false) Long patientId,
            @Parameter(description = "Statut : CONFIRME, ANNULE, ABSENT ou HONORE")
            @RequestParam(required = false) StatutRendezVous statut,
            @ParameterObject @PageableDefault(size = 20, sort = "debut") Pageable pageable) {
        return new PagedModel<>(service.lister(patientId, statut, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un rendez-vous")
    @ApiResponse(responseCode = "200", description = "Rendez-vous trouvé")
    @ApiResponse(responseCode = "404", description = "Rendez-vous introuvable")
    public RendezVousDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Prendre un rendez-vous",
            description = """
                    Réserve un créneau libre pour un patient. Un événement est publié, \
                    qui déclenche une notification de confirmation.

                    Refusé (409) si : le créneau est déjà réservé, le patient est bloqué \
                    (5 points de pénalité ou plus), il a déjà 3 rendez-vous à venir, \
                    ou il a déjà un rendez-vous sur le même horaire.""")
    @ApiResponse(responseCode = "201", description = "Rendez-vous confirmé")
    @ApiResponse(responseCode = "400", description = "Données invalides (patient ou créneau manquant)")
    @ApiResponse(responseCode = "404", description = "Patient ou créneau introuvable")
    @ApiResponse(responseCode = "409", description = "Créneau déjà pris, patient bloqué, quota atteint ou horaire en conflit")
    public RendezVousDto prendre(@Valid @RequestBody RendezVousRequest requete) {
        return service.prendre(requete);
    }

    @PostMapping("/{id}/annulation")
    @Operation(summary = "Annuler un rendez-vous",
            description = """
                    Libère le créneau. Une annulation à moins de 24 h du rendez-vous ajoute \
                    1 point de pénalité au patient. Si une liste d'attente existe pour ce créneau, \
                    le premier patient éligible obtient automatiquement le rendez-vous.""")
    @ApiResponse(responseCode = "200", description = "Rendez-vous annulé")
    @ApiResponse(responseCode = "404", description = "Rendez-vous introuvable")
    @ApiResponse(responseCode = "409", description = "Rendez-vous non confirmé ou déjà passé")
    public RendezVousDto annuler(@PathVariable Long id) {
        return service.annuler(id);
    }

    @PostMapping("/{id}/absence")
    @Operation(summary = "Marquer un patient absent",
            description = "Possible seulement après l'heure du rendez-vous. Ajoute 2 points de pénalité au patient.")
    @ApiResponse(responseCode = "200", description = "Absence enregistrée")
    @ApiResponse(responseCode = "404", description = "Rendez-vous introuvable")
    @ApiResponse(responseCode = "409", description = "Rendez-vous non confirmé ou pas encore passé")
    public RendezVousDto marquerAbsent(@PathVariable Long id) {
        return service.marquerAbsent(id);
    }

    @PostMapping("/attente")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "S'inscrire en liste d'attente",
            description = "Pour un créneau déjà réservé. Dès qu'il est libéré, le premier patient éligible de la file l'obtient.")
    @ApiResponse(responseCode = "201", description = "Inscription enregistrée, avec la position dans la file")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Patient ou créneau introuvable")
    @ApiResponse(responseCode = "409", description = "Créneau libre ou passé, déjà inscrit, patient bloqué, quota atteint ou horaire en conflit")
    public AttenteDto inscrireAttente(@Valid @RequestBody RendezVousRequest requete) {
        return service.inscrireAttente(requete);
    }

    @PostMapping("/rappels")
    @Operation(summary = "Déclencher les rappels J-1",
            description = "Envoie un rappel pour chaque rendez-vous confirmé dans les 24 prochaines heures qui n'en a pas "
                    + "encore reçu. Exécuté automatiquement toutes les heures ; cet endpoint sert aux tests.")
    @ApiResponse(responseCode = "200", description = "Nombre de rappels envoyés")
    public Map<String, Integer> declencherRappels() {
        return Map.of("rappelsEnvoyes", service.envoyerRappels());
    }
}