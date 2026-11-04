package sn.isi.clinique_api.catalogue.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import sn.isi.clinique_api.catalogue.api.CreneauRequest;
import sn.isi.clinique_api.shared.ConflitException;
import sn.isi.clinique_api.shared.RequeteInvalideException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreneauServiceTest {

    @Mock
    CreneauRepository repository;
    @Mock
    MedecinRepository medecinRepository;

    CreneauService service;

    final Medecin medecin = new Medecin("Diallo", "Awa", new Specialite("Cardiologie"));

    @BeforeEach
    void preparer() {
        service = new CreneauService(repository, medecinRepository);
    }

    private Creneau unCreneauFutur() {
        LocalDateTime debut = LocalDateTime.now().plusDays(2);
        Creneau creneau = new Creneau(medecin, debut, debut.plusMinutes(30));
        when(repository.findById(1L)).thenReturn(Optional.of(creneau));
        return creneau;
    }

    @Test
    void reserver_marqueLeCreneauCommeOccupe() {
        Creneau creneau = unCreneauFutur();

        service.reserver(1L);

        assertThat(creneau.isLibre()).isFalse();
    }

    @Test
    void reserver_refuseUnCreneauDejaOccupe() {
        Creneau creneau = unCreneauFutur();
        creneau.setLibre(false);

        assertThatThrownBy(() -> service.reserver(1L))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("déjà réservé");
    }

    @Test
    void liberer_remetLeCreneauEnLibre() {
        Creneau creneau = unCreneauFutur();
        creneau.setLibre(false);

        service.liberer(1L);

        assertThat(creneau.isLibre()).isTrue();
    }

    @Test
    void creer_refuseUneFinAvantLeDebut() {
        when(medecinRepository.findById(1L)).thenReturn(Optional.of(medecin));
        LocalDateTime debut = LocalDateTime.now().plusDays(2);

        assertThatThrownBy(() -> service.creer(new CreneauRequest(1L, debut, debut.minusMinutes(30))))
                .isInstanceOf(RequeteInvalideException.class)
                .hasMessageContaining("après son début");

        verify(repository, never()).save(any());
    }

    @Test
    void creer_refuseUnCreneauQuiChevaucheCeluiDuMemeMedecin() {
        when(medecinRepository.findById(1L)).thenReturn(Optional.of(medecin));
        when(repository.existeChevauchement(any(), any(), any(), any())).thenReturn(true);
        LocalDateTime debut = LocalDateTime.now().plusDays(2);

        assertThatThrownBy(() -> service.creer(new CreneauRequest(1L, debut, debut.plusMinutes(30))))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("chevauche");

        verify(repository, never()).save(any());
    }
}