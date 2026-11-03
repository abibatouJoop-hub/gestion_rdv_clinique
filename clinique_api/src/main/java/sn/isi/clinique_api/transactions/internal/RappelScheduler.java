package sn.isi.clinique_api.transactions.internal;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class RappelScheduler {

    private final RendezVousService service;

    RappelScheduler(RendezVousService service) {
        this.service = service;
    }

    /** Toutes les heures pile. */
    @Scheduled(cron = "0 0 * * * *")
    void envoyerRappels() {
        service.envoyerRappels();
    }
}