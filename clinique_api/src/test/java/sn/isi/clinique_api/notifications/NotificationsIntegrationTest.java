package sn.isi.clinique_api.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.jayway.jsonpath.JsonPath;

import sn.isi.clinique_api.IntegrationTestBase;

class NotificationsIntegrationTest extends IntegrationTestBase {

    /** Les notifications arrivent en asynchrone : on réessaie jusqu'à 10 secondes. */
    private void attendreNotification(long patientId, String type) {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                mvc.perform(get("/api/notifications").param("patientId", String.valueOf(patientId)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[*].type", hasItem(type))));
    }

    @Test
    void prendreUnRendezVous_creeUneNotificationDeConfirmation() throws Exception {
        long patient = creerPatient();
        prendreRendezVous(patient, creerCreneau(creerMedecin(), jourFutur(9)))
                .andExpect(status().isCreated());

        attendreNotification(patient, "RDV_CONFIRME");
    }

    @Test
    void annulerUnRendezVous_creeUneNotificationDAnnulation() throws Exception {
        long patient = creerPatient();
        long rendezVous = idDe(prendreRendezVous(patient, creerCreneau(creerMedecin(), jourFutur(9)))
                .andExpect(status().isCreated()));
        attendreNotification(patient, "RDV_CONFIRME");

        mvc.perform(post("/api/rendez-vous/{id}/annulation", rendezVous)).andExpect(status().isOk());

        attendreNotification(patient, "RDV_ANNULE");
    }

    @Test
    void uneReservationRefusee_neCreeAucuneNotification() throws Exception {
        long creneau = creerCreneau(creerMedecin(), jourFutur(9));
        long patientA = creerPatient();
        long patientB = creerPatient();
        prendreRendezVous(patientA, creneau).andExpect(status().isCreated());
        attendreNotification(patientA, "RDV_CONFIRME");   // point de synchronisation

        prendreRendezVous(patientB, creneau).andExpect(status().isConflict());

        // la transaction a échoué : l'événement n'a jamais été publié
        mvc.perform(get("/api/notifications").param("patientId", String.valueOf(patientB)))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void marquerUneNotificationCommeLue_laRetireDesNonLues() throws Exception {
        long patient = creerPatient();
        prendreRendezVous(patient, creerCreneau(creerMedecin(), jourFutur(9)))
                .andExpect(status().isCreated());
        attendreNotification(patient, "RDV_CONFIRME");

        String json = mvc.perform(get("/api/notifications").param("patientId", String.valueOf(patient)))
                .andReturn().getResponse().getContentAsString();
        long notification = ((Number) JsonPath.read(json, "$.content[0].id")).longValue();

        mvc.perform(patch("/api/notifications/{id}/lue", notification))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lue").value(true));

        mvc.perform(get("/api/notifications").param("patientId", String.valueOf(patient))
                        .param("nonLues", "true"))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void lesRappelsJ1_creentUneNotificationEtMarquentLeRendezVous() throws Exception {
        long patient = creerPatient();
        long creneau = creerCreneau(creerMedecin(), LocalDateTime.now().plusHours(3));   // dans moins de 24 h
        long rendezVous = idDe(prendreRendezVous(patient, creneau).andExpect(status().isCreated()));

        mvc.perform(post("/api/rendez-vous/rappels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rappelsEnvoyes", greaterThanOrEqualTo(1)));

        attendreNotification(patient, "RDV_RAPPEL");
        // l'indicateur empêche d'envoyer le même rappel deux fois
        assertThat(jdbc.queryForObject("select rappel_envoye from rendez_vous where id = ?",
                Boolean.class, rendezVous)).isTrue();
    }
}