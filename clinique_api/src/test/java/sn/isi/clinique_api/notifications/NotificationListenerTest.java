package sn.isi.clinique_api.notifications.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.isi.clinique_api.acteurs.api.PatientApi;
import sn.isi.clinique_api.acteurs.api.PatientDto;
import sn.isi.clinique_api.notifications.api.TypeNotification;
import sn.isi.clinique_api.transactions.api.RendezVousAnnule;
import sn.isi.clinique_api.transactions.api.RendezVousPris;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 11, 25, 9, 30);

    @Mock NotificationService service;
    @Mock PatientApi patients;
    @InjectMocks NotificationListener listener;

    private PatientDto aminata() {
        return new PatientDto(1L, "Diallo", "Aminata", "aminata@mail.com", "771234567", 0, false);
    }

    @Test
    void rendezVousPrisNotifieLePatientEtLeMedecin() {
        when(patients.trouverSiExiste(1L)).thenReturn(Optional.of(aminata()));

        listener.surRendezVousPris(new RendezVousPris(5L, 1L, DEBUT, 7L));

        verify(service).enregistrer(eq(1L), eq(TypeNotification.RDV_CONFIRME), contains("est confirmé"));
        verify(service).enregistrerPourMedecin(eq(7L), eq(TypeNotification.RDV_CONFIRME), contains("Aminata Diallo"));
    }

    @Test
    void sansMedecinSeulLePatientEstNotifie() {
        listener.surRendezVousPris(new RendezVousPris(5L, 1L, DEBUT));

        verify(service).enregistrer(eq(1L), eq(TypeNotification.RDV_CONFIRME), anyString());
        verify(service, never()).enregistrerPourMedecin(any(), any(), any());
    }

    @Test
    void annulationNotifieLePatientEtLeMedecin() {
        when(patients.trouverSiExiste(1L)).thenReturn(Optional.of(aminata()));

        listener.surRendezVousAnnule(new RendezVousAnnule(5L, 1L, DEBUT, 7L));

        verify(service).enregistrer(eq(1L), eq(TypeNotification.RDV_ANNULE), contains("annulé"));
        verify(service).enregistrerPourMedecin(eq(7L), eq(TypeNotification.RDV_ANNULE), contains("annulé"));
    }
}