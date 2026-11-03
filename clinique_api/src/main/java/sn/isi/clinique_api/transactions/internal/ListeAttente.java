package sn.isi.clinique_api.transactions.internal;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "liste_attente")
public class ListeAttente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "creneau_id", nullable = false)
    private Long creneauId;

    @Column(name = "inscrit_le", nullable = false)
    private LocalDateTime inscritLe;

    protected ListeAttente() {
        // requis par JPA
    }

    public ListeAttente(Long patientId, Long creneauId) {
        this.patientId = patientId;
        this.creneauId = creneauId;
        this.inscritLe = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public Long getCreneauId() {
        return creneauId;
    }
}