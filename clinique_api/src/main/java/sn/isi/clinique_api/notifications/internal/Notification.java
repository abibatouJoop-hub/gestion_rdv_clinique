package sn.isi.clinique_api.notifications.internal;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import sn.isi.clinique_api.notifications.api.TypeNotification;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TypeNotification type;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false)
    private boolean lue = false;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe;

    protected Notification() {
        // requis par JPA
    }

    public Notification(Long patientId, TypeNotification type, String message) {
        this.patientId = patientId;
        this.type = type;
        this.message = message;
        this.lue = false;
        this.creeLe = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public TypeNotification getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isLue() {
        return lue;
    }

    public void marquerLue() {
        this.lue = true;
    }

    public LocalDateTime getCreeLe() {
        return creeLe;
    }
}