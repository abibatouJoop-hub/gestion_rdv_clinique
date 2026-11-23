package sn.isi.clinique_api.securite.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    SecurityFilterChain chaineDeSecurite(HttpSecurity http, JwtService jwtService,
                                         ReponsesSecurite reponses) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/connexion", "/v3/api-docs/**",
                                "/swagger-ui/**", "/swagger-ui.html", "/actuator/health").permitAll()
                        .requestMatchers("/api/auth/moi").authenticated()
                        .requestMatchers("/api/utilisateurs/**").hasRole("ADMIN")

                        // la liste d'attente d'abord : règle plus précise que /api/rendez-vous/**
                        .requestMatchers("/api/rendez-vous/attente/**").hasAnyRole("ADMIN", "SECRETAIRE")
                        .requestMatchers(HttpMethod.GET, "/api/rendez-vous/**").hasAnyRole("ADMIN", "SECRETAIRE", "MEDECIN")
                        .requestMatchers(HttpMethod.POST, "/api/rendez-vous/*/absence").hasAnyRole("ADMIN", "SECRETAIRE", "MEDECIN")
                        .requestMatchers("/api/rendez-vous/**").hasAnyRole("ADMIN", "SECRETAIRE")

                        .requestMatchers(HttpMethod.GET, "/api/patients/*").hasAnyRole("ADMIN", "SECRETAIRE", "MEDECIN")
                        .requestMatchers("/api/patients/**").hasAnyRole("ADMIN", "SECRETAIRE")

                        .requestMatchers(HttpMethod.GET, "/api/specialites/**", "/api/medecins/**", "/api/creneaux/**").authenticated()
                        .requestMatchers("/api/specialites/**", "/api/medecins/**", "/api/creneaux/**").hasAnyRole("ADMIN", "SECRETAIRE")

                        .requestMatchers("/api/notifications/**").hasAnyRole("ADMIN", "SECRETAIRE", "MEDECIN")
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(reponses::nonAuthentifie)
                        .accessDeniedHandler(reponses::interdit))
                .addFilterBefore(new JwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Évite le mot de passe aléatoire créé par défaut : l'authentification passe par AuthService. */
    @Bean
    UserDetailsService aucunUtilisateurEnMemoire() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }
}