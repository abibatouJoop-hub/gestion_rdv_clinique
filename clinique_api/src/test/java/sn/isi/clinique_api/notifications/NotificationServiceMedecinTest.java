package sn.isi.clinique_api.notifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.isi.clinique_api.notifications.api.TypeNotification;
import sn.isi.clinique_api.notifications.internal.Notification;
import sn.isi.clinique_api.notifications.internal.NotificationRepository;
import sn.isi.clinique_api.notifications.internal.NotificationService;
import sn.isi.clinique_api.shared.NotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceMedecinTest {

    @Mock
    NotificationRepository repository;
    @InjectMocks
    NotificationService service;

    @Test
    void medecinMarqueSaPropreNotificationCommeLue() {
        Notification n = Notification.pourMedecin(7L, TypeNotification.RDV_CONFIRME, "Nouveau rendez-vous");
        when(repository.findById(1L)).thenReturn(Optional.of(n));

        assertThat(service.marquerLue(1L, 7L).lue()).isTrue();
    }

    @Test
    void medecinNePeutPasMarquerLaNotificationDUnConfrere() {
        Notification n = Notification.pourMedecin(7L, TypeNotification.RDV_CONFIRME, "Nouveau rendez-vous");
        when(repository.findById(1L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.marquerLue(1L, 8L)).isInstanceOf(NotFoundException.class);
        assertThat(n.isLue()).isFalse();
    }

    @Test
    void medecinNePeutPasMarquerUneNotificationDePatient() {
        Notification n = new Notification(1L, TypeNotification.RDV_CONFIRME, "Votre rendez-vous");
        when(repository.findById(1L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.marquerLue(1L, 7L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void listePourUnMedecinTransmetSonIdentifiantAuRepository() {
        when(repository.rechercher(isNull(), eq(7L), eq(false), any())).thenReturn(Page.empty());

        service.lister(null, 7L, false, Pageable.unpaged());

        verify(repository).rechercher(isNull(), eq(7L), eq(false), any());
    }
}