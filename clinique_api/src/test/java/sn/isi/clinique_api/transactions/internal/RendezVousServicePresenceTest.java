package sn.isi.clinique_api.transactions.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import sn.isi.clinique_api.acteurs.api.PatientApi;
import sn.isi.clinique_api.catalogue.api.CreneauApi;
import sn.isi.clinique_api.shared.NotFoundException;
import sn.isi.clinique_api.transactions.api.RendezVousDto;
import sn.isi.clinique_api.transactions.api.StatutRendezVous;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RendezVousServicePresenceTest {

    private static final long MEDECIN_DU_RDV = 7L;

    @Mock
    RendezVousRepository repository;
    @Mock
    ListeAttenteRepository attenteRepository;
    @Mock
    PatientApi patients;
    @Mock
    CreneauApi creneaux;
    @Mock
    ApplicationEventPublisher evenements;

    RendezVousService service;

    @BeforeEach
    void preparer() {
        service = new RendezVousService(repository, attenteRepository, patients, creneaux, evenements);
    }

    private RendezVous unRdvPasse() {
        LocalDateTime debut = LocalDateTime.now().minusHours(2);
        RendezVous rdv = new RendezVous(1L, 10L, MEDECIN_DU_RDV, debut, debut.plusMinutes(30));
        when(repository.findById(5L)).thenReturn(Optional.of(rdv));
        return rdv;
    }

    @Test
    void medecin_peutMarquerPresentSonPropreRendezVous() {
        unRdvPasse();

        RendezVousDto resultat = service.marquerPresent(5L, MEDECIN_DU_RDV);

        assertThat(resultat.statut()).isEqualTo(StatutRendezVous.HONORE);
    }

    @Test
    void medecin_ne_peutPasMarquerPresentLeRendezVousDUnConfrere() {
        RendezVous rdv = unRdvPasse();

        assertThatThrownBy(() -> service.marquerPresent(5L, 99L))
                .isInstanceOf(NotFoundException.class);

        assertThat(rdv.getStatut()).isEqualTo(StatutRendezVous.CONFIRME);   // rien n'a changé
    }
}