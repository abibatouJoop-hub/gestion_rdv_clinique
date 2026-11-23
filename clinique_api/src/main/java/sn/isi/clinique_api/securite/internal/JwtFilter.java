// JwtFilter.java
package sn.isi.clinique_api.securite.internal;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.isi.clinique_api.securite.api.UtilisateurConnecte;

import java.io.IOException;
import java.util.List;

class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String entete = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (entete != null && entete.startsWith("Bearer ")) {
            try {
                UtilisateurConnecte u = jwtService.lire(entete.substring(7));
                var authentification = new UsernamePasswordAuthenticationToken(
                        u, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.role().name())));
                SecurityContextHolder.getContext().setAuthentication(authentification);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext(); // jeton invalide : le point d'entrée renverra 401
            }
        }
        chain.doFilter(request, response);
    }
}