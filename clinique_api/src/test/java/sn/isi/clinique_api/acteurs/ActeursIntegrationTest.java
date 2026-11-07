package sn.isi.clinique_api.acteurs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import sn.isi.clinique_api.IntegrationTestBase;

class ActeursIntegrationTest extends IntegrationTestBase {

    private static String patientJson(String nom, String email) {
        return "{\"nom\":\"%s\",\"prenom\":\"Fatou\",\"email\":\"%s\"}".formatted(nom, email);
    }

    @Test
    void unNouveauPatient_aZeroPointDePenaliteEtNEstPasBloque() throws Exception {
        long id = creerPatient();

        mvc.perform(get("/api/patients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsPenalite").value(0))
                .andExpect(jsonPath("$.bloque").value(false));
    }

    @Test
    void deuxPatientsAvecLeMemeEmail_donnentUnConflit() throws Exception {
        String corps = patientJson("Ndiaye", "doublon-" + unique() + "@mail.com");

        mvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void unEmailInvalide_donneUneErreur400AvecLeDetailDuChamp() throws Exception {
        mvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson("Ndiaye", "pas-un-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void laRechercheTrouveUnPatientParUnFragmentDeSonNom() throws Exception {
        String nom = "Rech" + unique();
        mvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson(nom, nom.toLowerCase() + "@mail.com")))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/patients").param("recherche", nom))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void unPatientInconnu_donneUneErreur404() throws Exception {
        mvc.perform(get("/api/patients/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}