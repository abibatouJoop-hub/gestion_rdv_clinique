package sn.isi.clinique_api.notifications.internal;

import java.time.format.DateTimeFormatter;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import sn.isi.clinique_api.notifications.api.TypeNotification;
import sn.isi.clinique_api.transactions.api.RappelRendezVous;
import sn.isi.clinique_api.transactions.api.RendezVousAnnule;
import sn.isi.clinique_api.transactions.api.RendezVousPris;

@Component
class NotificationListener {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    private final NotificationService service;

    NotificationListener(NotificationService service) {
        this.service = service;
    }

    @ApplicationModuleListener
    void surRendezVousPris(RendezVousPris evenement) {
        service.enregistrer(evenement.patientId(), TypeNotification.RDV_CONFIRME,
                "Votre rendez-vous du " + evenement.debut().format(FORMAT) + " est confirmé.");
    }

    @ApplicationModuleListener
    void surRendezVousAnnule(RendezVousAnnule evenement) {
        service.enregistrer(evenement.patientId(), TypeNotification.RDV_ANNULE,
                "Votre rendez-vous du " + evenement.debut().format(FORMAT) + " a été annulé.");
    }

    @ApplicationModuleListener
    void surRappel(RappelRendezVous evenement) {
        service.enregistrer(evenement.patientId(), TypeNotification.RDV_RAPPEL,
                "Rappel : vous avez rendez-vous le " + evenement.debut().format(FORMAT) + ".");
    }
}