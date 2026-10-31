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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import sn.isi.clinique_api.catalogue.api.MedecinDto;
import sn.isi.clinique_api.catalogue.api.MedecinRequest;

@RestController
@RequestMapping("/api/medecins")
public class MedecinController {

    private final MedecinService service;

    public MedecinController(MedecinService service) {
        this.service = service;
    }

    @GetMapping
    public PagedModel<MedecinDto> rechercher(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) Long specialiteId,
            @ParameterObject @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return new PagedModel<>(service.rechercher(nom, specialiteId, pageable));
    }

    @GetMapping("/{id}")
    public MedecinDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedecinDto creer(@Valid @RequestBody MedecinRequest requete) {
        return service.creer(requete);
    }

    @PutMapping("/{id}")
    public MedecinDto modifier(@PathVariable Long id, @Valid @RequestBody MedecinRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}