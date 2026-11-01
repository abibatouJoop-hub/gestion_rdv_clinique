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

import jakarta.validation.Valid;
import sn.isi.clinique_api.acteurs.api.PatientDto;
import sn.isi.clinique_api.acteurs.api.PatientRequest;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    @GetMapping
    public PagedModel<PatientDto> rechercher(
            @RequestParam(required = false) String recherche,
            @ParameterObject @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return new PagedModel<>(service.rechercher(recherche, pageable));
    }

    @GetMapping("/{id}")
    public PatientDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto creer(@Valid @RequestBody PatientRequest requete) {
        return service.creer(requete);
    }

    @PutMapping("/{id}")
    public PatientDto modifier(@PathVariable Long id, @Valid @RequestBody PatientRequest requete) {
        return service.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}