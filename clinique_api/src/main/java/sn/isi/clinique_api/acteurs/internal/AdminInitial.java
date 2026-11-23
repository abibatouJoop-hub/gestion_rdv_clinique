package sn.isi.clinique_api.acteurs.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sn.isi.clinique_api.acteurs.api.Role;

@Component
class AdminInitial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitial.class);

    private final UtilisateurRepository repository;
    private final PasswordEncoder encoder;
    private final String email;
    private final String motDePasse;

    AdminInitial(UtilisateurRepository repository,
                 PasswordEncoder encoder,
                 @Value("${app.admin.email:}") String email,
                 @Value("${app.admin.mot-de-passe:}") String motDePasse) {
        this.repository = repository;
        this.encoder = encoder;
        this.email = email;
        this.motDePasse = motDePasse;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.existsByRole(Role.ADMIN)) {
            return;
        }
        if (email.isBlank() || motDePasse.isBlank()) {
            log.warn("Aucun administrateur et ADMIN_EMAIL / ADMIN_PASSWORD non définis : aucun compte créé");
            return;
        }
        repository.save(new Utilisateur(email.trim().toLowerCase(), encoder.encode(motDePasse), Role.ADMIN, null));
        log.info("Compte administrateur initial créé : {}", email);
    }
}