package sn.isi.clinique_api.transactions.api;

import java.time.LocalDateTime;

public record RendezVousPris(Long rdvId, Long patientId, LocalDateTime debut) {
    public static record RendezVousAnnule(Long rdvId, Long patientId, LocalDateTime debut) {
    }
}