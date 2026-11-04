package sn.isi.clinique_api.transactions.internal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.isi.clinique_api.acteurs.api.PatientApi;
import sn.isi.clinique_api.acteurs.api.PatientDto;
import sn.isi.clinique_api.catalogue.api.CreneauApi;
import sn.isi.clinique_api.catalogue.api.CreneauDto;
import sn.isi.clinique_api.shared.ConflitException;
import sn.isi.clinique_api.shared.NotFoundException;
import sn.isi.clinique_api.transactions.api.*;

@Service
@Transactional(readOnly = true)
public class RendezVousService {

    static final int QUOTA_MAX = 3;
    static final int HEURES_SANS_PENALITE = 24;
    static final int PENALITE_ANNULATION_TARDIVE = 1;
    static final int PENALITE_ABSENCE = 2;

    private final RendezVousRepository repository;
    private final ListeAttenteRepository attenteRepository;
    private final PatientApi patients;      // API publique du module acteurs
    private final CreneauApi creneaux;      // API publique du module catalogue
    private final ApplicationEventPublisher evenements;

    public RendezVousService(RendezVousRepository repository,
                             ListeAttenteRepository attenteRepository,
                             PatientApi patients,
                             CreneauApi creneaux,
                             ApplicationEventPublisher evenements) {
        this.repository = repository;
        this.attenteRepository = attenteRepository;
        this.patients = patients;
        this.creneaux = creneaux;
        this.evenements = evenements;
    }

    // =====================================================================
    // Consultation
    // =====================================================================

    public Page<RendezVousDto> lister(Long patientId, StatutRendezVous statut, Pageable pageable) {
        boolean filtrer = (statut != null);
        StatutRendezVous valeur = filtrer ? statut : StatutRendezVous.CONFIRME;
        return repository.rechercher(patientId, filtrer, valeur, pageable).map(this::versDto);
    }

    public RendezVousDto trouver(Long id) {
        return versDto(chercher(id));
    }

    // =====================================================================
    // F2 : prendre un rendez-vous
    // =====================================================================

    @Transactional
    public RendezVousDto prendre(RendezVousRequest requete) {
        PatientDto patient = patients.trouver(requete.patientId());   // 404 si absent
        CreneauDto creneau = creneaux.trouver(requete.creneauId());   // 404 si absent

        if (!creneau.libre()) {
            throw new ConflitException(
                    "Ce créneau est déjà réservé : vous pouvez vous inscrire en liste d'attente");
        }
        String motif = motifRefus(patient, creneau.debut(), creneau.fin());
        if (motif != null) {
            throw new ConflitException(motif);
        }

        creneaux.reserver(creneau.id());  // dernier garde-fou (accès concurrents)

        RendezVous rdv = repository.save(new RendezVous(patient.id(), creneau.id(),
                creneau.medecinId(), creneau.debut(), creneau.fin()));
        evenements.publishEvent(new RendezVousPris(rdv.getId(), patient.id(), rdv.getDebut()));
        return versDto(rdv);
    }

    // =====================================================================
    // F3 : annuler (avec pénalité si tardif) et marquer une absence
    // =====================================================================

    @Transactional
    public RendezVousDto annuler(Long id) {
        RendezVous rdv = chercher(id);
        if (rdv.getStatut() != StatutRendezVous.CONFIRME) {
            throw new ConflitException("Seul un rendez-vous confirmé peut être annulé");
        }
        LocalDateTime maintenant = LocalDateTime.now();
        if (!rdv.getDebut().isAfter(maintenant)) {
            throw new ConflitException("Ce rendez-vous est déjà passé");
        }

        boolean tardive = rdv.getDebut().isBefore(maintenant.plusHours(HEURES_SANS_PENALITE));

        rdv.setStatut(StatutRendezVous.ANNULE);
        // IMPORTANT : on écrit tout de suite l'annulation en base. Sans ce flush,
        // Hibernate exécuterait le nouvel INSERT de la liste d'attente AVANT cet UPDATE
        // et l'index unique "un seul rendez-vous confirmé par créneau" serait violé.
        repository.flush();

        if (tardive) {
            patients.ajouterPenalite(rdv.getPatientId(), PENALITE_ANNULATION_TARDIVE);
        }
        creneaux.liberer(rdv.getCreneauId());
        evenements.publishEvent(new RendezVousAnnule(rdv.getId(), rdv.getPatientId(), rdv.getDebut()));

        promouvoirListeAttente(rdv);
        return versDto(rdv);
    }

    @Transactional
    public RendezVousDto marquerAbsent(Long id) {
        RendezVous rdv = chercher(id);
        if (rdv.getStatut() != StatutRendezVous.CONFIRME) {
            throw new ConflitException("Seul un rendez-vous confirmé peut être marqué absent");
        }
        if (rdv.getDebut().isAfter(LocalDateTime.now())) {
            throw new ConflitException("Ce rendez-vous n'a pas encore eu lieu");
        }
        rdv.setStatut(StatutRendezVous.ABSENT);
        patients.ajouterPenalite(rdv.getPatientId(), PENALITE_ABSENCE);
        return versDto(rdv);
    }

    // =====================================================================
    // F4 : liste d'attente
    // =====================================================================

    @Transactional
    public AttenteDto inscrireAttente(RendezVousRequest requete) {
        PatientDto patient = patients.trouver(requete.patientId());
        CreneauDto creneau = creneaux.trouver(requete.creneauId());

        if (creneau.libre()) {
            throw new ConflitException("Ce créneau est libre : prenez directement rendez-vous");
        }
        if (!creneau.debut().isAfter(LocalDateTime.now())) {
            throw new ConflitException("Ce créneau est déjà passé");
        }
        if (attenteRepository.existsByPatientIdAndCreneauId(patient.id(), creneau.id())) {
            throw new ConflitException("Ce patient est déjà en liste d'attente pour ce créneau");
        }
        String motif = motifRefus(patient, creneau.debut(), creneau.fin());
        if (motif != null) {
            throw new ConflitException(motif);
        }

        ListeAttente entree = attenteRepository.save(new ListeAttente(patient.id(), creneau.id()));
        return new AttenteDto(entree.getId(), patient.id(), creneau.id(), position(entree));
    }

    /** Appelée après une annulation : le premier patient éligible de la file obtient le créneau. */
    private void promouvoirListeAttente(RendezVous annule) {
        List<ListeAttente> file =
                attenteRepository.findByCreneauIdOrderByInscritLeAscIdAsc(annule.getCreneauId());

        for (ListeAttente entree : file) {
            Optional<PatientDto> candidat = patients.trouverSiExiste(entree.getPatientId());
            if (candidat.isEmpty()) {
                attenteRepository.delete(entree);   // patient supprimé entre-temps
                continue;
            }
            if (motifRefus(candidat.get(), annule.getDebut(), annule.getFin()) != null) {
                continue;                           // bloqué, quota atteint ou conflit : au suivant
            }

            creneaux.reserver(annule.getCreneauId());
            RendezVous nouveau = repository.save(new RendezVous(candidat.get().id(),
                    annule.getCreneauId(), annule.getMedecinId(), annule.getDebut(), annule.getFin()));
            attenteRepository.delete(entree);
            evenements.publishEvent(
                    new RendezVousPris(nouveau.getId(), candidat.get().id(), nouveau.getDebut()));
            return;
        }
    }

    // =====================================================================
    // Méthodes internes
    // =====================================================================

    /** Retourne le motif du refus, ou null si le patient peut prendre ce créneau. */
    private String motifRefus(PatientDto patient, LocalDateTime debut, LocalDateTime fin) {
        if (patient.bloque()) {
            return "Ce patient est bloqué : " + patient.pointsPenalite()
                    + " points de pénalité (seuil : " + PatientApi.SEUIL_BLOCAGE + ")";
        }
        long aVenir = repository.countByPatientIdAndStatutAndDebutAfter(
                patient.id(), StatutRendezVous.CONFIRME, LocalDateTime.now());
        if (aVenir >= QUOTA_MAX) {
            return "Quota atteint : " + QUOTA_MAX + " rendez-vous à venir au maximum par patient";
        }
        if (repository.existeChevauchement(patient.id(), StatutRendezVous.CONFIRME, debut, fin)) {
            return "Ce patient a déjà un rendez-vous sur cet horaire";
        }
        return null;
    }

    private int position(ListeAttente entree) {
        List<ListeAttente> file =
                attenteRepository.findByCreneauIdOrderByInscritLeAscIdAsc(entree.getCreneauId());
        for (int i = 0; i < file.size(); i++) {
            if (file.get(i).getId().equals(entree.getId())) {
                return i + 1;
            }
        }
        return file.size();
    }

    private RendezVous chercher(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rendez-vous introuvable : " + id));
    }

    private RendezVousDto versDto(RendezVous r) {
        return new RendezVousDto(r.getId(), r.getPatientId(), r.getCreneauId(), r.getMedecinId(),
                r.getDebut(), r.getFin(), r.getStatut());
    }

    // =====================================================================
    // Rappels J-1
    // =====================================================================

    /** Envoie un rappel pour chaque rendez-vous confirmé qui a lieu dans les 24 prochaines heures. */
    @Transactional
    public int envoyerRappels() {
        LocalDateTime maintenant = LocalDateTime.now();
        List<RendezVous> aRappeler = repository.findByStatutAndRappelEnvoyeFalseAndDebutBetween(
                StatutRendezVous.CONFIRME, maintenant, maintenant.plusHours(24));

        for (RendezVous rdv : aRappeler) {
            rdv.marquerRappelEnvoye();
            evenements.publishEvent(
                    new RappelRendezVous(rdv.getId(), rdv.getPatientId(), rdv.getDebut()));
        }
        return aRappeler.size();
    }
}