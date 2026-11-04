package sn.isi.clinique_api.acteurs.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import sn.isi.clinique_api.acteurs.api.PatientRequest;
import sn.isi.clinique_api.shared.ConflitException;
import sn.isi.clinique_api.shared.NotFoundException;
import sn.isi.clinique_api.shared.RequeteInvalideException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PatientServiceTest {

    @Mock
    PatientRepository repository;

    PatientService service;

    @BeforeEach
    void preparer() {
        service = new PatientService(repository);
    }

    private Patient unPatient() {
        Patient patient = new Patient("Ndiaye", "Fatou", "fatou@mail.com", null);
        when(repository.findById(1L)).thenReturn(Optional.of(patient));
        return patient;
    }

    @Test
    void unPatientEstBloqueAPartirDeCinqPointsDePenalite() {
        Patient patient = unPatient();

        patient.ajouterPoints(4);
        assertThat(service.trouver(1L).bloque()).isFalse();

        patient.ajouterPoints(1);
        assertThat(service.trouver(1L).bloque()).isTrue();
    }

    @Test
    void ajouterPenalite_cumuleLesPoints() {
        unPatient();

        service.ajouterPenalite(1L, 1);
        service.ajouterPenalite(1L, 2);

        assertThat(service.trouver(1L).pointsPenalite()).isEqualTo(3);
    }

    @Test
    void ajouterPenalite_refuseUnNombreDePointsNul() {
        assertThatThrownBy(() -> service.ajouterPenalite(1L, 0))
                .isInstanceOf(RequeteInvalideException.class);

        verify(repository, never()).findById(any());
    }

    @Test
    void creer_refuseUnEmailDejaUtilise() {
        when(repository.existsByEmailIgnoreCase("fatou@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(
                new PatientRequest("Ndiaye", "Fatou", "fatou@mail.com", null)))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("existe déjà");

        verify(repository, never()).save(any());
    }

    @Test
    void trouver_leveUneErreur404SiLePatientNExistePas() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouver(99L))
                .isInstanceOf(NotFoundException.class);
    }
}