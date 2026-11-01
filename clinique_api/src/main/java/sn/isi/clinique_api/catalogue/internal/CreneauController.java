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

import jakarta.validation.Valid;
import sn.isi.clinique_api.catalogue.api.CreneauDto;
import sn.isi.clinique_api.catalogue.api.CreneauRequest;

@RestController
@RequestMapping("/api/creneaux")
public class CreneauController {

    private final CreneauService service;

    public CreneauController(CreneauService service) {
        this.service = service;
    }

    @GetMapping
    public PagedModel<CreneauDto> lister(
            @RequestParam(required = false) Long medecinId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Boolean libre,
            @ParameterObject @PageableDefault(size = 20, sort = "debut") Pageable pageable) {
        return new PagedModel<>(service.lister(medecinId, date, libre, pageable));
    }

    @GetMapping("/{id}")
    public CreneauDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreneauDto creer(@Valid @RequestBody CreneauRequest requete) {
        return service.creer(requete);
    }

    @PutMapping("/{id}")
    public CreneauDto modifier(@PathVariable Long id, @Valid @RequestBody CreneauRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}