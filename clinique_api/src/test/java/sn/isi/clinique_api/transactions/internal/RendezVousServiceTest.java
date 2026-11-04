package sn.isi.clinique_api.transactions.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import sn.isi.clinique_api.acteurs.api.PatientApi;
import sn.isi.clinique_api.acteurs.api.PatientDto;
import sn.isi.clinique_api.catalogue.api.CreneauApi;
import sn.isi.clinique_api.catalogue.api.CreneauDto;
import sn.isi.clinique_api.shared.ConflitException;
import sn.isi.clinique_api.transactions.api.RendezVousAnnule;
import sn.isi.clinique_api.transactions.api.RendezVousDto;
import sn.isi.clinique_api.transactions.api.RendezVousPris;
import sn.isi.clinique_api.transactions.api.RendezVousRequest;
import sn.isi.clinique_api.transactions.api.StatutRendezVous;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RendezVousServiceTest {

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

    // ---------- outils pour fabriquer des données de test ----------

    private static PatientDto unPatient(long id, int points) {
        return new PatientDto(id, "Nom", "Prenom", "p" + id + "@mail.com", null,
                points, points >= PatientApi.SEUIL_BLOCAGE);
    }

    private static CreneauDto unCreneau(long id, boolean libre, LocalDateTime debut) {
        return new CreneauDto(id, 7L, "Dr Test", debut, debut.plusMinutes(30), libre);
    }

    private static RendezVous unRdv(long patientId, long creneauId, LocalDateTime debut) {
        return new RendezVous(patientId, creneauId, 7L, debut, debut.plusMinutes(30));
    }

    private void donnerPatientEtCreneau(PatientDto patient, CreneauDto creneau) {
        when(patients.trouver(patient.id())).thenReturn(patient);
        when(creneaux.trouver(creneau.id())).thenReturn(creneau);
    }

    // ---------- F2 : prendre un rendez-vous ----------

    @Test
    void prendre_ok_reserveLeCreneauEtPublieUnEvenement() {
        donnerPatientEtCreneau(unPatient(1, 0), unCreneau(10, true, LocalDateTime.now().plusDays(3)));
        when(repository.save(any(RendezVous.class))).thenAnswer(i -> i.getArgument(0));

        RendezVousDto resultat = service.prendre(new RendezVousRequest(1L, 10L));

        assertThat(resultat.statut()).isEqualTo(StatutRendezVous.CONFIRME);
        assertThat(resultat.patientId()).isEqualTo(1L);
        verify(creneaux).reserver(10L);
        verify(evenements).publishEvent(any(RendezVousPris.class));
    }

    @Test
    void prendre_refuseSiLeCreneauEstDejaReserve() {
        donnerPatientEtCreneau(unPatient(1, 0), unCreneau(10, false, LocalDateTime.now().plusDays(3)));

        assertThatThrownBy(() -> service.prendre(new RendezVousRequest(1L, 10L)))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("déjà réservé");

        verify(creneaux, never()).reserver(anyLong());
        verify(repository, never()).save(any());
    }

    @Test
    void prendre_refuseSiLePatientEstBloque() {
        donnerPatientEtCreneau(unPatient(1, 5), unCreneau(10, true, LocalDateTime.now().plusDays(3)));

        assertThatThrownBy(() -> service.prendre(new RendezVousRequest(1L, 10L)))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("bloqué");

        verify(repository, never()).save(any());
    }

    @Test
    void prendre_refuseSiLeQuotaDeTroisRendezVousEstAtteint() {
        donnerPatientEtCreneau(unPatient(1, 0), unCreneau(10, true, LocalDateTime.now().plusDays(3)));
        when(repository.countByPatientIdAndStatutAndDebutAfter(anyLong(), any(), any())).thenReturn(3L);

        assertThatThrownBy(() -> service.prendre(new RendezVousRequest(1L, 10L)))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("Quota");

        verify(repository, never()).save(any());
    }

    @Test
    void prendre_refuseSiLePatientADejaUnRendezVousSurCetHoraire() {
        donnerPatientEtCreneau(unPatient(1, 0), unCreneau(10, true, LocalDateTime.now().plusDays(3)));
        when(repository.existeChevauchement(anyLong(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.prendre(new RendezVousRequest(1L, 10L)))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("horaire");

        verify(repository, never()).save(any());
    }

    // ---------- F3 : annulation et pénalités ----------

    @Test
    void annuler_moinsDe24hAvant_ajouteUnPointDePenalite() {
        RendezVous rdv = unRdv(1, 10, LocalDateTime.now().plusHours(2));
        when(repository.findById(5L)).thenReturn(Optional.of(rdv));

        RendezVousDto resultat = service.annuler(5L);

        assertThat(resultat.statut()).isEqualTo(StatutRendezVous.ANNULE);
        verify(patients).ajouterPenalite(1L, 1);   // règle : annulation tardive = 1 point
        verify(creneaux).liberer(10L);
    }

    @Test
    void annuler_plusDe24hAvant_neDonneAucunePenalite() {
        RendezVous rdv = unRdv(1, 10, LocalDateTime.now().plusDays(3));
        when(repository.findById(5L)).thenReturn(Optional.of(rdv));

        service.annuler(5L);

        verify(patients, never()).ajouterPenalite(anyLong(), anyInt());
        verify(creneaux).liberer(10L);
    }

    @Test
    void annuler_refuseUnRendezVousDejaAnnule() {
        RendezVous rdv = unRdv(1, 10, LocalDateTime.now().plusDays(3));
        rdv.setStatut(StatutRendezVous.ANNULE);
        when(repository.findById(5L)).thenReturn(Optional.of(rdv));

        assertThatThrownBy(() -> service.annuler(5L))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("confirmé");

        verify(creneaux, never()).liberer(anyLong());
    }

    @Test
    void marquerAbsent_ajoute2PointsDePenalite() {
        RendezVous rdv = unRdv(1, 10, LocalDateTime.now().minusHours(2));
        when(repository.findById(5L)).thenReturn(Optional.of(rdv));

        RendezVousDto resultat = service.marquerAbsent(5L);

        assertThat(resultat.statut()).isEqualTo(StatutRendezVous.ABSENT);
        verify(patients).ajouterPenalite(1L, 2);   // règle : absence = 2 points
    }

    @Test
    void marquerAbsent_refuseSiLeRendezVousNaPasEncoreEuLieu() {
        RendezVous rdv = unRdv(1, 10, LocalDateTime.now().plusDays(1));
        when(repository.findById(5L)).thenReturn(Optional.of(rdv));

        assertThatThrownBy(() -> service.marquerAbsent(5L))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("pas encore eu lieu");

        verify(patients, never()).ajouterPenalite(anyLong(), anyInt());
    }

    // ---------- F4 : liste d'attente ----------

    @Test
    void annuler_donneLeCreneauAuPremierPatientDeLaListeDAttente() {
        RendezVous annule = unRdv(1, 10, LocalDateTime.now().plusDays(3));
        when(repository.findById(5L)).thenReturn(Optional.of(annule));
        ListeAttente entree = new ListeAttente(2L, 10L);
        when(attenteRepository.findByCreneauIdOrderByInscritLeAscIdAsc(10L)).thenReturn(List.of(entree));
        when(patients.trouverSiExiste(2L)).thenReturn(Optional.of(unPatient(2, 0)));
        when(repository.save(any(RendezVous.class))).thenAnswer(i -> i.getArgument(0));

        service.annuler(5L);

        ArgumentCaptor<RendezVous> sauvegarde = ArgumentCaptor.forClass(RendezVous.class);
        verify(repository).save(sauvegarde.capture());
        assertThat(sauvegarde.getValue().getPatientId()).isEqualTo(2L);
        assertThat(sauvegarde.getValue().getCreneauId()).isEqualTo(10L);
        verify(creneaux).reserver(10L);
        verify(attenteRepository).delete(entree);

        ArgumentCaptor<Object> publies = ArgumentCaptor.forClass(Object.class);
        verify(evenements, times(2)).publishEvent(publies.capture());
        assertThat(publies.getAllValues())
                .hasAtLeastOneElementOfType(RendezVousAnnule.class)
                .hasAtLeastOneElementOfType(RendezVousPris.class);
    }

    @Test
    void annuler_ignoreLePatientBloqueEtServeLeSuivantDeLaFile() {
        RendezVous annule = unRdv(1, 10, LocalDateTime.now().plusDays(3));
        when(repository.findById(5L)).thenReturn(Optional.of(annule));
        ListeAttente bloque = new ListeAttente(2L, 10L);
        ListeAttente eligible = new ListeAttente(3L, 10L);
        when(attenteRepository.findByCreneauIdOrderByInscritLeAscIdAsc(10L))
                .thenReturn(List.of(bloque, eligible));
        when(patients.trouverSiExiste(2L)).thenReturn(Optional.of(unPatient(2, 5)));
        when(patients.trouverSiExiste(3L)).thenReturn(Optional.of(unPatient(3, 0)));
        when(repository.save(any(RendezVous.class))).thenAnswer(i -> i.getArgument(0));

        service.annuler(5L);

        ArgumentCaptor<RendezVous> sauvegarde = ArgumentCaptor.forClass(RendezVous.class);
        verify(repository).save(sauvegarde.capture());
        assertThat(sauvegarde.getValue().getPatientId()).isEqualTo(3L);
        verify(attenteRepository).delete(eligible);
        verify(attenteRepository, never()).delete(bloque);   // le patient bloqué reste en file
    }

    @Test
    void annuler_retireDeLaFileUnPatientSupprimeEtServeLeSuivant() {
        RendezVous annule = unRdv(1, 10, LocalDateTime.now().plusDays(3));
        when(repository.findById(5L)).thenReturn(Optional.of(annule));
        ListeAttente disparu = new ListeAttente(2L, 10L);
        ListeAttente suivant = new ListeAttente(3L, 10L);
        when(attenteRepository.findByCreneauIdOrderByInscritLeAscIdAsc(10L))
                .thenReturn(List.of(disparu, suivant));
        when(patients.trouverSiExiste(2L)).thenReturn(Optional.empty());
        when(patients.trouverSiExiste(3L)).thenReturn(Optional.of(unPatient(3, 0)));
        when(repository.save(any(RendezVous.class))).thenAnswer(i -> i.getArgument(0));

        service.annuler(5L);

        verify(attenteRepository).delete(disparu);
        verify(attenteRepository).delete(suivant);
        ArgumentCaptor<RendezVous> sauvegarde = ArgumentCaptor.forClass(RendezVous.class);
        verify(repository).save(sauvegarde.capture());
        assertThat(sauvegarde.getValue().getPatientId()).isEqualTo(3L);
    }

    @Test
    void inscrireAttente_refuseSiLeCreneauEstEncoreLibre() {
        donnerPatientEtCreneau(unPatient(1, 0), unCreneau(10, true, LocalDateTime.now().plusDays(3)));

        assertThatThrownBy(() -> service.inscrireAttente(new RendezVousRequest(1L, 10L)))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("libre");

        verify(attenteRepository, never()).save(any());
    }
}