package sn.isi.clinique_api.acteurs.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.isi.clinique_api.acteurs.api.Role;

import java.util.Optional;

interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRole(Role role);
    boolean existsByMedecinId(Long medecinId);
}