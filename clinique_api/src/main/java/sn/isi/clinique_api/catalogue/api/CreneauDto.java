package sn.isi.clinique_api.catalogue.api;

import java.time.LocalDateTime;

public record CreneauDto(Long id, Long medecinId, String medecinNom,
                         LocalDateTime debut, LocalDateTime fin, boolean libre) {
}