package sn.isi.clinique_api.catalogue.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.isi.clinique_api.catalogue.api.SpecialiteDto;
import sn.isi.clinique_api.catalogue.api.SpecialiteRequest;
import sn.isi.clinique_api.shared.ConflitException;
import sn.isi.clinique_api.shared.NotFoundException;

@Service
@Transactional(readOnly = true)
public class SpecialiteService {

    private final SpecialiteRepository repository;

    public SpecialiteService(SpecialiteRepository repository) {
        this.repository = repository;
    }

    public Page<SpecialiteDto> lister(Pageable pageable) {
        return repository.findAll(pageable).map(this::versDto);
    }

    public SpecialiteDto trouver(Long id) {
        return versDto(chercher(id));
    }

    @Transactional
    public SpecialiteDto creer(SpecialiteRequest requete) {
        String nom = requete.nom().trim();
        if (repository.existsByNomIgnoreCase(nom)) {
            throw new ConflitException("Cette spécialité existe déjà : " + nom);
        }
        return versDto(repository.save(new Specialite(nom)));
    }

    @Transactional
    public SpecialiteDto modifier(Long id, SpecialiteRequest requete) {
        Specialite specialite = chercher(id);
        String nom = requete.nom().trim();
        if (repository.existsByNomIgnoreCaseAndIdNot(nom, id)) {
            throw new ConflitException("Cette spécialité existe déjà : " + nom);
        }
        specialite.setNom(nom);
        return versDto(specialite);
    }

    @Transactional
    public void supprimer(Long id) {
        repository.delete(chercher(id));
    }

    private Specialite chercher(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Spécialité introuvable : " + id));
    }

    private SpecialiteDto versDto(Specialite s) {
        return new SpecialiteDto(s.getId(), s.getNom());
    }
}