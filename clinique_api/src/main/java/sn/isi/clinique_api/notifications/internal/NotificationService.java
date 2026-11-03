package sn.isi.clinique_api.notifications.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.isi.clinique_api.notifications.api.NotificationDto;
import sn.isi.clinique_api.notifications.api.TypeNotification;
import sn.isi.clinique_api.shared.NotFoundException;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public Page<NotificationDto> lister(Long patientId, boolean seulementNonLues, Pageable pageable) {
        return repository.rechercher(patientId, seulementNonLues, pageable).map(this::versDto);
    }

    @Transactional
    public NotificationDto marquerLue(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification introuvable : " + id));
        notification.marquerLue();
        return versDto(notification);
    }

    @Transactional
    public void enregistrer(Long patientId, TypeNotification type, String message) {
        repository.save(new Notification(patientId, type, message));
    }

    private NotificationDto versDto(Notification n) {
        return new NotificationDto(n.getId(), n.getPatientId(), n.getType(),
                n.getMessage(), n.isLue(), n.getCreeLe());
    }
}