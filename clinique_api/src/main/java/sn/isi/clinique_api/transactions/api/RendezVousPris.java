package sn.isi.clinique_api.transactions.api;

import java.time.LocalDateTime;

public record RendezVousPris(Long rendezVousId, Long patientId, LocalDateTime debut, Long medecinId) {

    /** Compatibilité : sans médecin, aucune notification n'est envoyée au médecin. */
    public RendezVousPris(Long rendezVousId, Long patientId, LocalDateTime debut) {
        this(rendezVousId, patientId, debut, null);
    }
}