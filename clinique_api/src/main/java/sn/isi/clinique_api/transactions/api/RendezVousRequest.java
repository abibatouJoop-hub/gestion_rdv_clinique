package sn.isi.clinique_api.transactions.api;

import jakarta.validation.constraints.NotNull;

public record RendezVousRequest(
        @NotNull(message = "Le patient est obligatoire")
        Long patientId,

        @NotNull(message = "Le créneau est obligatoire")
        Long creneauId) {
}