package sn.isi.clinique_api.securite;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import sn.isi.clinique_api.IntegrationTestBase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@AutoConfigureMockMvc   // ici les filtres de sécurité sont actifs
class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void preparer() {
        jdbc.update("delete from utilisateur where email = 'secretaire@test.sn'");
        jdbc.update("insert into utilisateur(email, mot_de_passe, role) values (?, ?, 'SECRETAIRE')",
                "secretaire@test.sn", encoder.encode("Secret123!"));
    }

    private String connexion(String mdp) throws Exception {
        return mvc.perform(post("/api/auth/connexion").contentType("application/json")
                        .content("{\"email\":\"secretaire@test.sn\",\"motDePasse\":\"" + mdp + "\"}"))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void sansJetonOnRecoit401() throws Exception {
        mvc.perform(get("/api/specialites"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"));
    }

    @Test
    void mauvaisMotDePasseDonne401() throws Exception {
        mvc.perform(post("/api/auth/connexion").contentType("application/json")
                        .content("{\"email\":\"secretaire@test.sn\",\"motDePasse\":\"faux\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("IDENTIFIANTS_INVALIDES"));
    }

    @Test
    void connexionPuisAccesAvecLeJeton() throws Exception {
        String token = JsonPath.read(connexion("Secret123!"), "$.token");
        mvc.perform(get("/api/specialites").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/auth/moi").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SECRETAIRE"));
    }

    @Test
    void jetonInvalideDonne401() throws Exception {
        mvc.perform(get("/api/specialites").header("Authorization", "Bearer n-importe-quoi"))
                .andExpect(status().isUnauthorized());
    }
}