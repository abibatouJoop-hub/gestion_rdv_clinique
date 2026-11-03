package sn.isi.clinique_api.shared;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI cliniqueOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API Clinique - prise de rendez-vous")
                .version("1.0")
                .description("""
                        Monolithe modulaire Spring Boot : catalogue, acteurs, transactions, notifications.

                        Toutes les erreurs (400, 404, 409) ont le même format JSON :
                        code, message, horodatage, details."""));
    }

    @Bean
    GroupedOpenApi catalogueApi() {
        return GroupedOpenApi.builder()
                .group("catalogue")
                .packagesToScan("sn.isi.clinique_api.catalogue")
                .build();
    }

    @Bean
    GroupedOpenApi acteursApi() {
        return GroupedOpenApi.builder()
                .group("acteurs")
                .packagesToScan("sn.isi.clinique_api.acteurs")
                .build();
    }

    @Bean
    GroupedOpenApi transactionsApi() {
        return GroupedOpenApi.builder()
                .group("transactions")
                .packagesToScan("sn.isi.clinique_api.transactions")
                .build();
    }

    @Bean
    GroupedOpenApi notificationsApi() {
        return GroupedOpenApi.builder()
                .group("notifications")
                .packagesToScan("sn.isi.clinique_api.notifications")
                .build();
    }
}