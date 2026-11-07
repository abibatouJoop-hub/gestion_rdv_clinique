package sn.isi.clinique_api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    // Un seul PostgreSQL pour toute la suite de tests (supprimé automatiquement à la fin)
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void brancherLaBase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    protected static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JdbcTemplate jdbc;

    // ---------- outils pour fabriquer des données ----------

    /** Texte aléatoire de 8 caractères, pour que chaque test ait ses propres données. */
    protected static String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /** Un jour dans 10 jours à l'heure donnée : toujours dans le futur, et à plus de 24 h. */
    protected static LocalDateTime jourFutur(int heure) {
        return LocalDate.now().plusDays(10).atTime(heure, 0);
    }

    /** Lit l'identifiant "id" dans la réponse JSON. */
    protected static long idDe(ResultActions resultat) throws Exception {
        String json = resultat.andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    protected long creerPatient() throws Exception {
        String corps = "{\"nom\":\"Test\",\"prenom\":\"Patient\",\"email\":\"p-%s@mail.com\",\"telephone\":\"770000000\"}"
                .formatted(unique());
        return idDe(mvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated()));
    }

    protected long creerMedecin(String nom) throws Exception {
        // la spécialité 1 (Médecine générale) vient de la migration de données de référence
        String corps = "{\"nom\":\"%s\",\"prenom\":\"Test\",\"specialiteId\":1}".formatted(nom);
        return idDe(mvc.perform(post("/api/medecins").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated()));
    }

    protected long creerMedecin() throws Exception {
        return creerMedecin("Docteur" + unique());
    }

    protected ResultActions posterCreneau(long medecinId, LocalDateTime debut, LocalDateTime fin) throws Exception {
        String corps = "{\"medecinId\":%d,\"debut\":\"%s\",\"fin\":\"%s\"}"
                .formatted(medecinId, debut.format(ISO), fin.format(ISO));
        return mvc.perform(post("/api/creneaux").contentType(MediaType.APPLICATION_JSON).content(corps));
    }

    protected long creerCreneau(long medecinId, LocalDateTime debut) throws Exception {
        return idDe(posterCreneau(medecinId, debut, debut.plusMinutes(30)).andExpect(status().isCreated()));
    }

    protected static String corpsRendezVous(long patientId, long creneauId) {
        return "{\"patientId\":%d,\"creneauId\":%d}".formatted(patientId, creneauId);
    }

    protected ResultActions prendreRendezVous(long patientId, long creneauId) throws Exception {
        return mvc.perform(post("/api/rendez-vous").contentType(MediaType.APPLICATION_JSON)
                .content(corpsRendezVous(patientId, creneauId)));
    }
}