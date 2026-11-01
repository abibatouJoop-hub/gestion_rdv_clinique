package sn.isi.clinique_api.acteurs.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.isi.clinique_api.acteurs.api.PatientApi;
import sn.isi.clinique_api.acteurs.api.PatientDto;
import sn.isi.clinique_api.acteurs.api.PatientRequest;
import sn.isi.clinique_api.shared.ConflitException;
import sn.isi.clinique_api.shared.NotFoundException;
import sn.isi.clinique_api.shared.RequeteInvalideException;

@Service
@Transactional(readOnly = true)
public class PatientService implements PatientApi {

    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    // ---------- consultation ----------

    public Page<PatientDto> rechercher(String critere, Pageable pageable) {
        String texte = (critere == null) ? "" : critere.trim();
        return repository.rechercher(texte, pageable).map(this::versDto);
    }

    @Override
    public PatientDto trouver(Long id) {
        return versDto(chercher(id));
    }

    // ---------- CRUD ----------

    @Transactional
    public PatientDto creer(PatientRequest requete) {
        String email = requete.email().trim();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitException("Un patient avec cet email existe déjà : " + email);
        }
        Patient patient = new Patient(requete.nom().trim(), requete.prenom().trim(),
                email, nettoyer(requete.telephone()));
        return versDto(repository.save(patient));
    }

    @Transactional
    public PatientDto modifier(Long id, PatientRequest requete) {
        Patient patient = chercher(id);
        String email = requete.email().trim();
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflitException("Un patient avec cet email existe déjà : " + email);
        }
        patient.setNom(requete.nom().trim());
        patient.setPrenom(requete.prenom().trim());
        patient.setEmail(email);
        patient.setTelephone(nettoyer(requete.telephone()));
        return versDto(patient);
    }

    @Transactional
    public void supprimer(Long id) {
        repository.delete(chercher(id));
    }

    // ---------- API publique (utilisée par le module transactions) ----------

    @Override
    @Transactional
    public void ajouterPenalite(Long id, int points) {
        if (points <= 0) {
            throw new RequeteInvalideException("Le nombre de points doit être strictement positif");
        }
        chercher(id).ajouterPoints(points);
    }

    // ---------- méthodes internes ----------

    private Patient chercher(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Patient introuvable : " + id));
    }

    private String nettoyer(String telephone) {
        if (telephone == null || telephone.isBlank()) {
            return null;
        }
        return telephone.trim();
    }

    private PatientDto versDto(Patient p) {
        return new PatientDto(p.getId(), p.getNom(), p.getPrenom(), p.getEmail(), p.getTelephone(),
                p.getPointsPenalite(), p.getPointsPenalite() >= SEUIL_BLOCAGE);
    }
}