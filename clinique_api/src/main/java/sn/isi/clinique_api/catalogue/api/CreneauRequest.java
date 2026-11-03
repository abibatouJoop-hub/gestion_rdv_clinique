package sn.isi.clinique_api.catalogue.api;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreneauRequest(
        @Schema(description = "Identifiant d'un médecin existant", example = "1")
        @NotNull(message = "Le médecin est obligatoire")
        Long medecinId,

        @Schema(description = "Début du créneau, dans le futur (AAAA-MM-JJTHH:MM:SS)",
                example = "2026-11-16T09:00:00")
        @NotNull(message = "La date de début est obligatoire")
        LocalDateTime debut,

        @Schema(description = "Fin du créneau, après le début", example = "2026-11-16T09:30:00")
        @NotNull(message = "La date de fin est obligatoire")
        LocalDateTime fin) {
}