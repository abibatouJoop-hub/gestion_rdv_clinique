package sn.isi.clinique_api.securite;

import org.junit.jupiter.api.Test;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import sn.isi.clinique_api.IntegrationTestBase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ComptesIntegrationTest extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void nettoyer() {
        jdbc.update("delete from utilisateur where email like '%@test.sn'");
    }

    private String jeton(String email, String role, Long medecinId) throws Exception {
        jdbc.update("insert into utilisateur(email, mot_de_passe, role, medecin_id) values (?, ?, ?, ?)",
                email, encoder.encode("Secret123!"), role, medecinId);
        String json = mvc.perform(post("/api/auth/connexion").contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"motDePasse\":\"Secret123!\"}"))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(json, "$.token");
    }

    private String corps(String email, String role, String medecinId) {
        return "{\"email\":\"" + email + "\",\"motDePasse\":\"Motdepasse1\",\"role\":\"" + role + "\""
                + (medecinId == null ? "" : ",\"medecinId\":" + medecinId) + "}";
    }

    @Test
    void secretaireNePeutPasCreerDeCompte() throws Exception {
        String jeton = jeton("secretaire@test.sn", "SECRETAIRE", null);
        mvc.perform(post("/api/utilisateurs").header("Authorization", jeton)
                        .contentType("application/json").content(corps("x@test.sn", "SECRETAIRE", null)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void adminCreeUnCompteSansExposerLeMotDePasse() throws Exception {
        String jeton = jeton("admin@test.sn", "ADMIN", null);
        mvc.perform(post("/api/utilisateurs").header("Authorization", jeton)
                        .contentType("application/json").content(corps("Nouvelle@test.sn", "SECRETAIRE", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nouvelle@test.sn"))
                .andExpect(jsonPath("$.role").value("SECRETAIRE"))
                .andExpect(jsonPath("$.motDePasse").doesNotExist());
    }

    @Test
    void compteMedecinExigeUnMedecinExistant() throws Exception {
        String jeton = jeton("admin@test.sn", "ADMIN", null);
        mvc.perform(post("/api/utilisateurs").header("Authorization", jeton)
                        .contentType("application/json").content(corps("m1@test.sn", "MEDECIN", null)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/utilisateurs").header("Authorization", jeton)
                        .contentType("application/json").content(corps("m2@test.sn", "MEDECIN", "999999")))
                .andExpect(status().isNotFound());
    }

    @Test
    void emailDejaUtiliseDonne409() throws Exception {
        String jeton = jeton("admin@test.sn", "ADMIN", null);
        mvc.perform(post("/api/utilisateurs").header("Authorization", jeton)
                        .contentType("application/json").content(corps("admin@test.sn", "SECRETAIRE", null)))
                .andExpect(status().isConflict());
    }

    @Test
    void medecinNAccedePasAuxPatientsMaisLitLesRendezVous() throws Exception {
        String jeton = jeton("medecin@test.sn", "MEDECIN", 424242L);
        mvc.perform(get("/api/patients").header("Authorization", jeton))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/rendez-vous").header("Authorization", jeton))
                .andExpect(status().isOk());
    }

    @Test
    void adminNePeutPasSeDesactiverLuiMeme() throws Exception {
        String jeton = jeton("admin@test.sn", "ADMIN", null);
        Long id = jdbc.queryForObject("select id from utilisateur where email = 'admin@test.sn'", Long.class);
        mvc.perform(patch("/api/utilisateurs/" + id + "/desactivation").header("Authorization", jeton))
                .andExpect(status().isConflict());
    }

    @Test
    void medecinNePeutPasVoirLaListeDAttente() throws Exception {
        String jeton = jeton("medecin@test.sn", "MEDECIN", 424242L);
        mvc.perform(get("/api/rendez-vous/attente").header("Authorization", jeton))
                .andExpect(status().isForbidden());
    }
    @Test
    void medecinNeVoitQueSesNotifications() throws Exception {
        jdbc.update("delete from notification where medecin_id = 424242 or patient_id = 999999");
        jdbc.update("insert into notification(medecin_id, type, message) values (424242, 'RDV_CONFIRME', 'pour le medecin')");
        jdbc.update("insert into notification(patient_id, type, message) values (999999, 'RDV_CONFIRME', 'pour le patient')");
        String jeton = jeton("medecin@test.sn", "MEDECIN", 424242L);

        mvc.perform(get("/api/notifications?patientId=999999").header("Authorization", jeton))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].message").value("pour le medecin"));
    }

    @Test
    void medecinNePeutPasMarquerLaNotificationDUnPatient() throws Exception {
        jdbc.update("delete from notification where medecin_id = 424242 or patient_id = 999999");
        jdbc.update("insert into notification(patient_id, type, message) values (999999, 'RDV_CONFIRME', 'pour le patient')");
        Long id = jdbc.queryForObject("select id from notification where patient_id = 999999", Long.class);
        String jeton = jeton("medecin@test.sn", "MEDECIN", 424242L);

        mvc.perform(patch("/api/notifications/" + id + "/lue").header("Authorization", jeton))
                .andExpect(status().isNotFound());
    }
}