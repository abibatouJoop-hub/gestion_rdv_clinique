package sn.isi.clinique_api.catalogue.internal;

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

import jakarta.validation.Valid;
import sn.isi.clinique_api.catalogue.api.SpecialiteDto;
import sn.isi.clinique_api.catalogue.api.SpecialiteRequest;

@RestController
@RequestMapping("/api/specialites")
public class SpecialiteController {

    private final SpecialiteService service;

    public SpecialiteController(SpecialiteService service) {
        this.service = service;
    }

    @GetMapping
    public PagedModel<SpecialiteDto> lister(@PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return new PagedModel<>(service.lister(pageable));
    }

    @GetMapping("/{id}")
    public SpecialiteDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SpecialiteDto creer(@Valid @RequestBody SpecialiteRequest requete) {
        return service.creer(requete);
    }

    @PutMapping("/{id}")
    public SpecialiteDto modifier(@PathVariable Long id, @Valid @RequestBody SpecialiteRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}