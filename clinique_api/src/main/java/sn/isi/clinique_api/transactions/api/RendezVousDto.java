package sn.isi.clinique_api.transactions.api;

import java.time.LocalDateTime;

public record RendezVousDto(Long id, Long patientId, Long creneauId, Long medecinId,
                            LocalDateTime debut, LocalDateTime fin, StatutRendezVous statut) {
}