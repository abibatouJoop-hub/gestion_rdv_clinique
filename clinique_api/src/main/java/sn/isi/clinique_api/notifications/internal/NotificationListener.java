package sn.isi.clinique_api.notifications.internal;

import java.time.format.DateTimeFormatter;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import sn.isi.clinique_api.acteurs.api.PatientApi;
import sn.isi.clinique_api.notifications.api.TypeNotification;
import sn.isi.clinique_api.transactions.api.RappelRendezVous;
import sn.isi.clinique_api.transactions.api.RendezVousAnnule;
import sn.isi.clinique_api.transactions.api.RendezVousPris;

@Component
class NotificationListener {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    private final NotificationService service;
    private final PatientApi patients;

    NotificationListener(NotificationService service, PatientApi patients) {
        this.service = service;
        this.patients = patients;
    }

    @ApplicationModuleListener
    void surRendezVousPris(RendezVousPris evenement) {
        String date = evenement.debut().format(FORMAT);
        service.enregistrer(evenement.patientId(), TypeNotification.RDV_CONFIRME,
                "Votre rendez-vous du " + date + " est confirmé.");
        if (evenement.medecinId() != null) {
            service.enregistrerPourMedecin(evenement.medecinId(), TypeNotification.RDV_CONFIRME,
                    "Nouveau rendez-vous le " + date + " avec " + nomPatient(evenement.patientId()) + ".");
        }
    }

    @ApplicationModuleListener
    void surRendezVousAnnule(RendezVousAnnule evenement) {
        String date = evenement.debut().format(FORMAT);
        service.enregistrer(evenement.patientId(), TypeNotification.RDV_ANNULE,
                "Votre rendez-vous du " + date + " a été annulé.");
        if (evenement.medecinId() != null) {
            service.enregistrerPourMedecin(evenement.medecinId(), TypeNotification.RDV_ANNULE,
                    "Le rendez-vous du " + date + " avec " + nomPatient(evenement.patientId()) + " a été annulé.");
        }
    }

    @ApplicationModuleListener
    void surRappel(RappelRendezVous evenement) {
        service.enregistrer(evenement.patientId(), TypeNotification.RDV_RAPPEL,
                "Rappel : vous avez rendez-vous le " + evenement.debut().format(FORMAT) + ".");
    }

    private String nomPatient(Long patientId) {
        return patients.trouverSiExiste(patientId)
                .map(p -> p.prenom() + " " + p.nom())
                .orElse("un patient");
    }
}