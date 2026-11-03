package sn.isi.clinique_api.catalogue.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpecialiteRequest(
        @Schema(description = "Nom de la spécialité (unique)", example = "Neurologie")
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom) {
}