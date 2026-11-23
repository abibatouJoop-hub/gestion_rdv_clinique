package sn.isi.clinique_api.transactions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.isi.clinique_api.acteurs.api.PatientApi;
import sn.isi.clinique_api.catalogue.api.CreneauApi;
import sn.isi.clinique_api.shared.NotFoundException;
import sn.isi.clinique_api.transactions.api.StatutRendezVous;
import sn.isi.clinique_api.transactions.internal.ListeAttenteRepository;
import sn.isi.clinique_api.transactions.internal.RendezVous;
import sn.isi.clinique_api.transactions.internal.RendezVousRepository;
import sn.isi.clinique_api.transactions.internal.RendezVousService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RendezVousServiceMedecinTest {

    @Mock
    RendezVousRepository repository;
    @Mock
    ListeAttenteRepository attenteRepository;
    @Mock PatientApi patients;
    @Mock CreneauApi creneaux;
    @Mock ApplicationEventPublisher evenements;

    RendezVousService service;
    RendezVous rdvDuMedecin7;

    @BeforeEach
    void init() {
        service = new RendezVousService(repository, attenteRepository, patients, creneaux, evenements);
        LocalDateTime debut = LocalDateTime.now().minusHours(2);
        rdvDuMedecin7 = new RendezVous(1L, 10L, 7L, debut, debut.plusMinutes(30));
    }

    @Test
    void medecinVoitSonPropreRendezVous() {
        when(repository.findById(5L)).thenReturn(Optional.of(rdvDuMedecin7));
        assertThat(service.trouver(5L, 7L).medecinId()).isEqualTo(7L);
    }

    @Test
    void medecinNeVoitPasLeRendezVousDUnConfrere() {
        when(repository.findById(5L)).thenReturn(Optional.of(rdvDuMedecin7));
        assertThatThrownBy(() -> service.trouver(5L, 8L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void medecinNePeutPasMarquerAbsentLePatientDUnConfrere() {
        when(repository.findById(5L)).thenReturn(Optional.of(rdvDuMedecin7));
        assertThatThrownBy(() -> service.marquerAbsent(5L, 8L)).isInstanceOf(NotFoundException.class);
        verify(patients, never()).ajouterPenalite(any(), anyInt());
    }

    @Test
    void listePourUnMedecinUtiliseLaRequeteFiltree() {
        when(repository.rechercherParMedecin(eq(7L), isNull(), eq(false),
                eq(StatutRendezVous.CONFIRME), any())).thenReturn(Page.empty());
        service.lister(null, null, 7L, Pageable.unpaged());
        verify(repository, never()).rechercher(any(), anyBoolean(), any(), any());
    }
}