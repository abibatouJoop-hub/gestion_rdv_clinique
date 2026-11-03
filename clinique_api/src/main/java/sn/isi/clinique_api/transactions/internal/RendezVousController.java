package sn.isi.clinique_api.transactions.internal;

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

import jakarta.validation.Valid;
import sn.isi.clinique_api.transactions.api.AttenteDto;
import sn.isi.clinique_api.transactions.api.RendezVousDto;
import sn.isi.clinique_api.transactions.api.RendezVousRequest;
import sn.isi.clinique_api.transactions.api.StatutRendezVous;

@RestController
@RequestMapping("/api/rendez-vous")
public class RendezVousController {

    private final RendezVousService service;

    public RendezVousController(RendezVousService service) {
        this.service = service;
    }

    @GetMapping
    public PagedModel<RendezVousDto> lister(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) StatutRendezVous statut,
            @ParameterObject @PageableDefault(size = 20, sort = "debut") Pageable pageable) {
        return new PagedModel<>(service.lister(patientId, statut, pageable));
    }

    @GetMapping("/{id}")
    public RendezVousDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RendezVousDto prendre(@Valid @RequestBody RendezVousRequest requete) {
        return service.prendre(requete);
    }

    @PostMapping("/{id}/annulation")
    public RendezVousDto annuler(@PathVariable Long id) {
        return service.annuler(id);
    }

    @PostMapping("/{id}/absence")
    public RendezVousDto marquerAbsent(@PathVariable Long id) {
        return service.marquerAbsent(id);
    }

    @PostMapping("/attente")
    @ResponseStatus(HttpStatus.CREATED)
    public AttenteDto inscrireAttente(@Valid @RequestBody RendezVousRequest requete) {
        return service.inscrireAttente(requete);
    }
}