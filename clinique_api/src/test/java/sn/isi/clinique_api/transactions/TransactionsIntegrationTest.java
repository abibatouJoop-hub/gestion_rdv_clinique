package sn.isi.clinique_api.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import sn.isi.clinique_api.IntegrationTestBase;

class TransactionsIntegrationTest extends IntegrationTestBase {

    @Test
    void prendreUnRendezVous_leConfirmeEtOccupeLeCreneau() throws Exception {
        long patient = creerPatient();
        long creneau = creerCreneau(creerMedecin(), jourFutur(9));

        prendreRendezVous(patient, creneau)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("CONFIRME"))
                .andExpect(jsonPath("$.patientId").value(patient));

        mvc.perform(get("/api/creneaux/{id}", creneau))
                .andExpect(jsonPath("$.libre").value(false));
    }

    @Test
    void unCreneauDejaReserve_estRefuseAuSecondPatient() throws Exception {
        long creneau = creerCreneau(creerMedecin(), jourFutur(9));
        prendreRendezVous(creerPatient(), creneau).andExpect(status().isCreated());

        prendreRendezVous(creerPatient(), creneau)
                .andExpect(status().isConflict());
    }

    @Test
    void auDelaDeTroisRendezVousAVenir_lePatientEstRefuse() throws Exception {
        long medecin = creerMedecin();
        long patient = creerPatient();
        for (int heure = 9; heure <= 11; heure++) {
            prendreRendezVous(patient, creerCreneau(medecin, jourFutur(heure)))
                    .andExpect(status().isCreated());
        }

        prendreRendezVous(patient, creerCreneau(medecin, jourFutur(14)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Quota")));
    }

    @Test
    void annulerUnRendezVous_donneLeCreneauAuPremierDeLaListeDAttente() throws Exception {
        long creneau = creerCreneau(creerMedecin(), jourFutur(9));
        long patientA = creerPatient();
        long patientB = creerPatient();
        long rendezVousA = idDe(prendreRendezVous(patientA, creneau).andExpect(status().isCreated()));

        mvc.perform(post("/api/rendez-vous/attente").contentType(MediaType.APPLICATION_JSON)
                        .content(corpsRendezVous(patientB, creneau)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.position").value(1));

        mvc.perform(post("/api/rendez-vous/{id}/annulation", rendezVousA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULE"));

        // le patient B a obtenu le créneau automatiquement
        mvc.perform(get("/api/rendez-vous").param("patientId", String.valueOf(patientB))
                        .param("statut", "CONFIRME"))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        // le rendez-vous annulé plus de 24 h à l'avance ne coûte aucun point
        mvc.perform(get("/api/patients/{id}", patientA))
                .andExpect(jsonPath("$.pointsPenalite").value(0));
    }

    @Test
    void uneAnnulationATardive_ajouteUnPointDePenalite() throws Exception {
        long patient = creerPatient();
        long creneau = creerCreneau(creerMedecin(), LocalDateTime.now().plusHours(2));   // dans moins de 24 h
        long rendezVous = idDe(prendreRendezVous(patient, creneau).andExpect(status().isCreated()));

        mvc.perform(post("/api/rendez-vous/{id}/annulation", rendezVous))
                .andExpect(status().isOk());

        mvc.perform(get("/api/patients/{id}", patient))
                .andExpect(jsonPath("$.pointsPenalite").value(1));
    }

    @Test
    void unPatientBloque_nePeutPlusPrendreRendezVous() throws Exception {
        long patient = creerPatient();
        long creneau = creerCreneau(creerMedecin(), jourFutur(9));
        jdbc.update("update patient set points_penalite = 5 where id = ?", patient);

        prendreRendezVous(patient, creneau)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("5 points")));
    }

    @Test
    void marquerUneAbsence_ajouteDeuxPointsDePenalite() throws Exception {
        long patient = creerPatient();
        long creneau = creerCreneau(creerMedecin(), jourFutur(9));
        long rendezVous = idDe(prendreRendezVous(patient, creneau).andExpect(status().isCreated()));

        // l'API refuse les créneaux passés : on recule le rendez-vous directement en base
        LocalDateTime passe = LocalDateTime.now().minusHours(3);
        jdbc.update("update rendez_vous set debut = ?, fin = ? where id = ?", passe, passe.plusMinutes(30), rendezVous);

        mvc.perform(post("/api/rendez-vous/{id}/absence", rendezVous))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ABSENT"));

        mvc.perform(get("/api/patients/{id}", patient))
                .andExpect(jsonPath("$.pointsPenalite").value(2));
        assertThat(jdbc.queryForObject("select statut from rendez_vous where id = ?", String.class, rendezVous))
                .isEqualTo("ABSENT");
    }
}