package sn.isi.clinique_api.acteurs.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PatientRequest(
        @Schema(description = "Nom de famille", example = "Ndiaye")
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @Schema(description = "Prénom", example = "Fatou")
        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom,

        @Schema(description = "Adresse e-mail, unique", example = "fatou.ndiaye@mail.com")
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email n'est pas valide")
        @Size(max = 150, message = "L'email ne doit pas dépasser 150 caractères")
        String email,

        @Schema(description = "Numéro de téléphone (facultatif)", example = "771234567")
        @Size(max = 30, message = "Le téléphone ne doit pas dépasser 30 caractères")
        String telephone) {
}