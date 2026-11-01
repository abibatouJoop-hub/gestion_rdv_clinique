package sn.isi.clinique_api.catalogue.api;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record CreneauRequest(
        @NotNull(message = "Le médecin est obligatoire")
        Long medecinId,

        @NotNull(message = "La date de début est obligatoire")
        LocalDateTime debut,

        @NotNull(message = "La date de fin est obligatoire")
        LocalDateTime fin) {
}