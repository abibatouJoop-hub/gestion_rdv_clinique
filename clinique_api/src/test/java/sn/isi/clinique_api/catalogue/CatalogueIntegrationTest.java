package sn.isi.clinique_api.catalogue;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import sn.isi.clinique_api.IntegrationTestBase;

class CatalogueIntegrationTest extends IntegrationTestBase {

    @Test
    void lesSpecialitesDeReferenceSontChargeesParFlyway() throws Exception {
        mvc.perform(get("/api/specialites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(6)));
    }

    @Test
    void creerUnMedecin_puisLeRetrouverParSonNom() throws Exception {
        String nom = "Recherche" + unique();
        long id = creerMedecin(nom);

        mvc.perform(get("/api/medecins").param("nom", nom))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].specialiteId").value(1));
    }

    @Test
    void unCreneauQuiChevaucheUnAutreDuMemeMedecin_estRefuse() throws Exception {
        long medecin = creerMedecin();
        LocalDateTime debut = jourFutur(9);
        creerCreneau(medecin, debut);                       // 09:00 - 09:30

        posterCreneau(medecin, debut.plusMinutes(15), debut.plusMinutes(45))   // 09:15 - 09:45
                .andExpect(status().isConflict());
    }

    @Test
    void unCreneauDontLaFinPrecedeLeDebut_estRefuse() throws Exception {
        long medecin = creerMedecin();
        LocalDateTime debut = jourFutur(9);

        posterCreneau(medecin, debut, debut.minusMinutes(30))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechercheDeMedecins_filtreParDisponibiliteEtParDate() throws Exception {
        String nom = "Dispo" + unique();
        long medecin = creerMedecin(nom);
        LocalDateTime debut = jourFutur(9);
        creerCreneau(medecin, debut);

        // disponible : il a un créneau libre
        mvc.perform(get("/api/medecins").param("nom", nom).param("disponible", "true"))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        // le bon jour : il apparaît
        mvc.perform(get("/api/medecins").param("nom", nom).param("date", debut.toLocalDate().toString()))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        // le lendemain : il n'a aucun créneau
        mvc.perform(get("/api/medecins").param("nom", nom)
                        .param("date", debut.toLocalDate().plusDays(1).toString()))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }
}