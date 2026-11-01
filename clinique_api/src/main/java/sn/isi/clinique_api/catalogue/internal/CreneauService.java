package sn.isi.clinique_api.catalogue.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.isi.clinique_api.catalogue.api.CreneauApi;
import sn.isi.clinique_api.catalogue.api.CreneauDto;
import sn.isi.clinique_api.catalogue.api.CreneauRequest;
import sn.isi.clinique_api.shared.ConflitException;
import sn.isi.clinique_api.shared.NotFoundException;
import sn.isi.clinique_api.shared.RequeteInvalideException;

@Service
@Transactional(readOnly = true)
public class CreneauService implements CreneauApi {

    private static final LocalDateTime DEBUT_MIN = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime FIN_MAX = LocalDateTime.of(9999, 12, 31, 0, 0);

    private final CreneauRepository repository;
    private final MedecinRepository medecinRepository;

    public CreneauService(CreneauRepository repository, MedecinRepository medecinRepository) {
        this.repository = repository;
        this.medecinRepository = medecinRepository;
    }

    // ---------- consultation ----------

    public Page<CreneauDto> lister(Long medecinId, LocalDate date, Boolean libre, Pageable pageable) {
        LocalDateTime min = (date != null) ? date.atStartOfDay() : DEBUT_MIN;
        LocalDateTime max = (date != null) ? date.plusDays(1).atStartOfDay() : FIN_MAX;
        boolean filtrerLibre = (libre != null);
        boolean valeurLibre = Boolean.TRUE.equals(libre);
        return repository.rechercher(medecinId, min, max, filtrerLibre, valeurLibre, pageable)
                .map(this::versDto);
    }

    @Override
    public CreneauDto trouver(Long id) {
        return versDto(chercher(id));
    }

    // ---------- CRUD ----------

    @Transactional
    public CreneauDto creer(CreneauRequest requete) {
        Medecin medecin = chercherMedecin(requete.medecinId());
        verifierHoraire(medecin.getId(), requete.debut(), requete.fin(), null);
        return versDto(repository.save(new Creneau(medecin, requete.debut(), requete.fin())));
    }

    @Transactional
    public CreneauDto modifier(Long id, CreneauRequest requete) {
        Creneau creneau = chercher(id);
        if (!creneau.isLibre()) {
            throw new ConflitException("Un créneau déjà réservé ne peut pas être modifié");
        }
        Medecin medecin = chercherMedecin(requete.medecinId());
        verifierHoraire(medecin.getId(), requete.debut(), requete.fin(), id);
        creneau.setMedecin(medecin);
        creneau.setDebut(requete.debut());
        creneau.setFin(requete.fin());
        return versDto(creneau);
    }

    @Transactional
    public void supprimer(Long id) {
        Creneau creneau = chercher(id);
        if (!creneau.isLibre()) {
            throw new ConflitException("Un créneau déjà réservé ne peut pas être supprimé");
        }
        repository.delete(creneau);
    }

    // ---------- API publique (utilisée par le module transactions) ----------

    @Override
    @Transactional
    public void reserver(Long id) {
        Creneau creneau = chercher(id);
        if (!creneau.isLibre()) {
            throw new ConflitException("Ce créneau est déjà réservé");
        }
        if (creneau.getDebut().isBefore(LocalDateTime.now())) {
            throw new ConflitException("Ce créneau est déjà passé");
        }
        creneau.setLibre(false);
    }

    @Override
    @Transactional
    public void liberer(Long id) {
        chercher(id).setLibre(true);
    }

    // ---------- méthodes internes ----------

    private void verifierHoraire(Long medecinId, LocalDateTime debut, LocalDateTime fin, Long idExclu) {
        if (!fin.isAfter(debut)) {
            throw new RequeteInvalideException("La fin du créneau doit être après son début");
        }
        if (!debut.isAfter(LocalDateTime.now())) {
            throw new RequeteInvalideException("Le créneau doit commencer dans le futur");
        }
        Long exclure = (idExclu == null) ? -1L : idExclu;
        if (repository.existeChevauchement(medecinId, debut, fin, exclure)) {
            throw new ConflitException("Ce médecin a déjà un créneau qui chevauche cet horaire");
        }
    }

    private Creneau chercher(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Créneau introuvable : " + id));
    }

    private Medecin chercherMedecin(Long id) {
        return medecinRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Médecin introuvable : " + id));
    }

    private CreneauDto versDto(Creneau c) {
        Medecin m = c.getMedecin();
        return new CreneauDto(c.getId(), m.getId(), m.getPrenom() + " " + m.getNom(),
                c.getDebut(), c.getFin(), c.isLibre());
    }
}