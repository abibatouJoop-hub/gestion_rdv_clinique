// ConnexionRequest.java
package sn.isi.clinique_api.securite.api;

import jakarta.validation.constraints.NotBlank;

public record ConnexionRequest(@NotBlank String email, @NotBlank String motDePasse) {
}