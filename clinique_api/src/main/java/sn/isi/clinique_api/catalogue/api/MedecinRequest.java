package sn.isi.clinique_api.catalogue.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MedecinRequest(
        @Schema(description = "Nom de famille", example = "Diallo")
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @Schema(description = "Prénom", example = "Awa")
        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom,

        @Schema(description = "Identifiant d'une spécialité existante", example = "1")
        @NotNull(message = "La spécialité est obligatoire")
        Long specialiteId) {
}