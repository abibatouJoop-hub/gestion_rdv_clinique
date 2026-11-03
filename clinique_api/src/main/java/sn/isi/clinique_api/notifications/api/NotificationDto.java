package sn.isi.clinique_api.notifications.api;

import java.time.LocalDateTime;

public record NotificationDto(Long id, Long patientId, TypeNotification type,
                              String message, boolean lue, LocalDateTime creeLe) {
}