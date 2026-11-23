package sn.isi.clinique_api.acteurs.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.isi.clinique_api.acteurs.api.Role;
import sn.isi.clinique_api.acteurs.api.UtilisateurDto;
import sn.isi.clinique_api.acteurs.api.UtilisateurRequest;
import sn.isi.clinique_api.catalogue.api.MedecinApi;
import sn.isi.clinique_api.shared.ConflitException;
import sn.isi.clinique_api.shared.NotFoundException;
import sn.isi.clinique_api.shared.RequeteInvalideException;

@Service
@Transactional
class UtilisateurGestionService {

    private final UtilisateurRepository repository;
    private final PasswordEncoder encoder;
    private final MedecinApi medecinApi;

    UtilisateurGestionService(UtilisateurRepository repository, PasswordEncoder encoder, MedecinApi medecinApi) {
        this.repository = repository;
        this.encoder = encoder;
        this.medecinApi = medecinApi;
    }

    UtilisateurDto creer(UtilisateurRequest r) {
        String email = r.email().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitException("Un compte existe déjà avec cet e-mail");
        }
        if (r.role() == Role.MEDECIN) {
            if (r.medecinId() == null) {
                throw new RequeteInvalideException("medecinId est obligatoire pour un compte MEDECIN");
            }
            if (!medecinApi.existe(r.medecinId())) {
                throw new NotFoundException("Médecin introuvable : " + r.medecinId());
            }
            if (repository.existsByMedecinId(r.medecinId())) {
                throw new ConflitException("Ce médecin possède déjà un compte");
            }
        } else if (r.medecinId() != null) {
            throw new RequeteInvalideException("medecinId n'est autorisé que pour le rôle MEDECIN");
        }
        Utilisateur u = repository.save(new Utilisateur(email, encoder.encode(r.motDePasse()), r.role(), r.medecinId()));
        return versDto(u);
    }

    @Transactional(readOnly = true)
    Page<UtilisateurDto> lister(Pageable pageable) {
        return repository.findAll(pageable).map(this::versDto);
    }

    UtilisateurDto desactiver(Long id, String emailAppelant) {
        Utilisateur u = trouver(id);
        if (u.getEmail().equalsIgnoreCase(emailAppelant)) {
            throw new ConflitException("Vous ne pouvez pas désactiver votre propre compte");
        }
        u.desactiver();
        return versDto(u);
    }

    UtilisateurDto activer(Long id) {
        Utilisateur u = trouver(id);
        u.activer();
        return versDto(u);
    }

    private Utilisateur trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Compte introuvable : " + id));
    }

    private UtilisateurDto versDto(Utilisateur u) {
        return new UtilisateurDto(u.getId(), u.getEmail(), u.getRole(), u.getMedecinId(), u.isActif());
    }
}