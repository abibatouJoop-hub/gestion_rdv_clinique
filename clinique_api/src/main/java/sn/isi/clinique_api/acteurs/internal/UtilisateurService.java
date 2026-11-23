package sn.isi.clinique_api.acteurs.internal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.isi.clinique_api.acteurs.api.UtilisateurApi;
import sn.isi.clinique_api.acteurs.api.UtilisateurAuthDto;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
class UtilisateurService implements UtilisateurApi {

    private final UtilisateurRepository repository;

    UtilisateurService(UtilisateurRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UtilisateurAuthDto> trouverActifParEmail(String email) {
        return repository.findByEmailIgnoreCase(email.trim())
                .filter(Utilisateur::isActif)
                .map(u -> new UtilisateurAuthDto(u.getId(), u.getEmail(), u.getMotDePasse(),
                        u.getRole(), u.getMedecinId()));
    }
}