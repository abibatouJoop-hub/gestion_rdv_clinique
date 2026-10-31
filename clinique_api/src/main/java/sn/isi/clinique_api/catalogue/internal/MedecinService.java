package sn.isi.clinique_api.catalogue.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.isi.clinique_api.catalogue.api.MedecinDto;
import sn.isi.clinique_api.catalogue.api.MedecinRequest;
import sn.isi.clinique_api.shared.NotFoundException;

@Service
@Transactional(readOnly = true)
public class MedecinService {

    private final MedecinRepository repository;
    private final SpecialiteRepository specialiteRepository;

    public MedecinService(MedecinRepository repository, SpecialiteRepository specialiteRepository) {
        this.repository = repository;
        this.specialiteRepository = specialiteRepository;
    }

    public Page<MedecinDto> rechercher(String nom, Long specialiteId, Pageable pageable) {
        // une chaîne vide correspond à "tous les noms" (évite un paramètre null dans la requête)
        String critere = (nom == null) ? "" : nom.trim();
        return repository.rechercher(critere, specialiteId, pageable).map(this::versDto);
    }

    public MedecinDto trouver(Long id) {
        return versDto(chercher(id));
    }

    @Transactional
    public MedecinDto creer(MedecinRequest requete) {
        Specialite specialite = chercherSpecialite(requete.specialiteId());
        Medecin medecin = new Medecin(requete.nom().trim(), requete.prenom().trim(), specialite);
        return versDto(repository.save(medecin));
    }

    @Transactional
    public MedecinDto modifier(Long id, MedecinRequest requete) {
        Medecin medecin = chercher(id);
        medecin.setNom(requete.nom().trim());
        medecin.setPrenom(requete.prenom().trim());
        medecin.setSpecialite(chercherSpecialite(requete.specialiteId()));
        return versDto(medecin);
    }

    @Transactional
    public void supprimer(Long id) {
        repository.delete(chercher(id));
        repository.flush(); // force l'erreur tout de suite si des créneaux sont liés (409)
    }

    private Medecin chercher(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Médecin introuvable : " + id));
    }

    private Specialite chercherSpecialite(Long id) {
        return specialiteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Spécialité introuvable : " + id));
    }

    private MedecinDto versDto(Medecin m) {
        return new MedecinDto(m.getId(), m.getNom(), m.getPrenom(),
                m.getSpecialite().getId(), m.getSpecialite().getNom());
    }
}