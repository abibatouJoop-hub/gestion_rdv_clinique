package sn.isi.clinique_api.acteurs.internal;

import jakarta.persistence.*;
import sn.isi.clinique_api.acteurs.api.Role;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 100)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "medecin_id")
    private Long medecinId;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe = LocalDateTime.now();

    protected Utilisateur() {
    }

    public Utilisateur(String email, String motDePasseHache, Role role, Long medecinId) {
        this.email = email;
        this.motDePasse = motDePasseHache;
        this.role = role;
        this.medecinId = medecinId;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getMotDePasse() { return motDePasse; }
    public Role getRole() { return role; }
    public Long getMedecinId() { return medecinId; }
    public boolean isActif() { return actif; }
    public LocalDateTime getCreeLe() { return creeLe; }

    public void desactiver() { this.actif = false; }
    public void activer() { this.actif = true; }
}