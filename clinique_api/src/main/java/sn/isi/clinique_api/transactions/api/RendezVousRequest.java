package sn.isi.clinique_api.transactions.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record RendezVousRequest(
        @Schema(description = "Identifiant d'un patient existant", example = "1")
        @NotNull(message = "Le patient est obligatoire")
        Long patientId,

        @Schema(description = "Identifiant d'un créneau existant", example = "1")
        @NotNull(message = "Le créneau est obligatoire")
        Long creneauId) {
}