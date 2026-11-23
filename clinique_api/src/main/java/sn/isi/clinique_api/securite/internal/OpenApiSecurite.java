// OpenApiSecurite.java : bouton « Authorize » dans Swagger et groupe « securite »
package sn.isi.clinique_api.securite.internal;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@OpenAPIDefinition(security = @SecurityRequirement(name = "bearerAuth"))
class OpenApiSecurite {

    @Bean
    GroupedOpenApi groupeSecurite() {
        return GroupedOpenApi.builder().group("securite").pathsToMatch("/api/auth/**").build();
    }
}