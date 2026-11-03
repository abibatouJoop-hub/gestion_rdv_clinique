package sn.isi.clinique_api.transactions.internal;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import sn.isi.clinique_api.transactions.api.StatutRendezVous;

@Entity
@Table(name = "rendez_vous")
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "creneau_id", nullable = false)
    private Long creneauId;

    @Column(name = "medecin_id", nullable = false)
    private Long medecinId;

    @Column(nullable = false)
    private LocalDateTime debut;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutRendezVous statut;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe;

    @Column(name = "rappel_envoye", nullable = false)
    private boolean rappelEnvoye = false;

    protected RendezVous() {
        // requis par JPA
    }

    public RendezVous(Long patientId, Long creneauId, Long medecinId,
                      LocalDateTime debut, LocalDateTime fin) {
        this.patientId = patientId;
        this.creneauId = creneauId;
        this.medecinId = medecinId;
        this.debut = debut;
        this.fin = fin;
        this.statut = StatutRendezVous.CONFIRME;
        this.creeLe = LocalDateTime.now();
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

    public Long getMedecinId() {
        return medecinId;
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public StatutRendezVous getStatut() {
        return statut;
    }

    public void setStatut(StatutRendezVous statut) {
        this.statut = statut;
    }

    public boolean isRappelEnvoye() {
        return rappelEnvoye;
    }

    public void marquerRappelEnvoye() {
        this.rappelEnvoye = true;
    }
}