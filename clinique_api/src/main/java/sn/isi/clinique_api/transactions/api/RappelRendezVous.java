package sn.isi.clinique_api.transactions.api;

import java.time.LocalDateTime;

public record RappelRendezVous(Long rdvId, Long patientId, LocalDateTime debut) {
}