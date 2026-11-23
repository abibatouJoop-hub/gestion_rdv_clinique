// JwtService.java
package sn.isi.clinique_api.securite.internal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sn.isi.clinique_api.acteurs.api.Role;
import sn.isi.clinique_api.acteurs.api.UtilisateurAuthDto;
import sn.isi.clinique_api.securite.api.UtilisateurConnecte;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
class JwtService {

    private final SecretKey cle;
    private final Duration duree;

    JwtService(@Value("${app.jwt.secret:}") String secret,
               @Value("${app.jwt.expiration-minutes:60}") long minutes) {
        byte[] octets = secret.getBytes(StandardCharsets.UTF_8);
        if (octets.length < 32) {
            throw new IllegalStateException("JWT_SECRET est absent ou fait moins de 32 caractères");
        }
        this.cle = Keys.hmacShaKeyFor(octets);
        this.duree = Duration.ofMinutes(minutes);
    }

    String generer(UtilisateurAuthDto u) {
        Instant maintenant = Instant.now();
        return Jwts.builder()
                .subject(u.email())
                .claim("id", u.id())
                .claim("role", u.role().name())
                .claim("medecinId", u.medecinId())
                .issuedAt(Date.from(maintenant))
                .expiration(Date.from(maintenant.plus(duree)))
                .signWith(cle)
                .compact();
    }

    long dureeEnSecondes() {
        return duree.toSeconds();
    }

    /** Lève une JwtException si le jeton est invalide ou expiré. */
    UtilisateurConnecte lire(String token) {
        Claims c = Jwts.parser().verifyWith(cle).build().parseSignedClaims(token).getPayload();
        Number medecin = c.get("medecinId", Number.class);
        return new UtilisateurConnecte(
                ((Number) c.get("id")).longValue(),
                c.getSubject(),
                Role.valueOf(c.get("role", String.class)),
                medecin == null ? null : medecin.longValue());
    }
}