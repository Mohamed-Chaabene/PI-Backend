package t.esprit.arctic.jobmatch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import t.esprit.arctic.jobmatch.security.JwtFilter;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // ── Endpoints publics GET ─────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/domaines").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/formations").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/formations/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/entretiens/public/tests").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/questions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/candidats/email/**").permitAll()

                        // ── Suggestions et proxy ──────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/suggestions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/proxy/**").permitAll()

                        // ── Partenaires ───────────────────────────────────────
                        .requestMatchers("/api/partenaires/**").permitAll()
                        .requestMatchers("/api/offres-partenaires/**").permitAll()

                        // ── Search / Users ────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/users/search").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/search/utilisateurs/nom").authenticated()

                        // ── Offres emploi ─────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/offres-emploi", "/api/offres-emploi/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/offres-emploi/mes-offres").hasAnyAuthority("ROLE_RECRUTEUR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/candidatures/offre/**").hasAnyAuthority("ROLE_RECRUTEUR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/offres-emploi/**").hasAnyAuthority("ROLE_RECRUTEUR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/offres-emploi/**").hasAnyAuthority("ROLE_RECRUTEUR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/offres-emploi/**").hasAnyAuthority("ROLE_RECRUTEUR", "ROLE_ADMIN")

                        // ── Feedbacks ─────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/feedbacks").hasAuthority("ROLE_CANDIDAT")
                        .requestMatchers(HttpMethod.GET, "/api/feedbacks/**").hasAnyAuthority("ROLE_ORGANISATEUR", "ROLE_CANDIDAT")

                        // ── Questions / Entretiens ────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/questions/entretien/**").hasAnyAuthority("ROLE_RECRUTEUR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/questions/entretien/*/ai-generate").hasAnyAuthority("ROLE_RECRUTEUR", "ROLE_ADMIN")

                        // ── Rôles admin ───────────────────────────────────────
                        .requestMatchers("/api/users/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // ── Rôles métier ──────────────────────────────────────
                        .requestMatchers("/api/candidat/**").hasAuthority("ROLE_CANDIDAT")
                        .requestMatchers("/api/recruteur/**").hasAuthority("ROLE_RECRUTEUR")
                        .requestMatchers("/api/recruteurs/**").hasAuthority("ROLE_RECRUTEUR")
                        .requestMatchers("/api/client-freelance/**").hasAuthority("ROLE_CLIENT_FREELANCE")
                        .requestMatchers("/api/organisateur/**").hasAuthority("ROLE_ORGANISATEUR")

                        // ── Événements ────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/participations/stats/**").hasAnyAuthority("ROLE_CANDIDAT", "CANDIDAT")
                        .requestMatchers(HttpMethod.GET, "/api/evenements/stats").hasAnyAuthority("ROLE_ORGANISATEUR", "ORGANISATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/evenements/admin/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/evenements/**").hasAnyAuthority("ROLE_CANDIDAT", "ROLE_ORGANISATEUR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/evenements/**").hasAuthority("ROLE_ORGANISATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/evenements").hasAuthority("ROLE_ORGANISATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/evenements/**").hasAuthority("ROLE_ORGANISATEUR")

                        // ── Participations ────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/participations").hasAnyAuthority("ROLE_CANDIDAT", "CANDIDAT")
                        .requestMatchers(HttpMethod.PUT, "/api/participations/**").hasAnyAuthority("ROLE_ORGANISATEUR", "ORGANISATEUR", "ROLE_CANDIDAT", "CANDIDAT")
                        .requestMatchers(HttpMethod.GET, "/api/participations/**").hasAnyAuthority("ROLE_ORGANISATEUR", "ORGANISATEUR", "ROLE_CANDIDAT", "CANDIDAT")

                        // ── Feedbacks Evenement ───────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/feedbacks-evenement").hasAnyAuthority("ROLE_CANDIDAT", "CANDIDAT")
                        .requestMatchers(HttpMethod.GET, "/api/feedbacks-evenement/**").hasAnyAuthority("ROLE_CANDIDAT", "CANDIDAT", "ROLE_ORGANISATEUR", "ORGANISATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/feedbacks-evenement/**").hasAnyAuthority("ROLE_CANDIDAT", "CANDIDAT")
                        .requestMatchers(HttpMethod.DELETE, "/api/feedbacks-evenement/**").hasAnyAuthority("ROLE_CANDIDAT", "CANDIDAT")
                        .requestMatchers(HttpMethod.GET, "/api/feedbacks-evenement/reputation").hasAnyAuthority("ROLE_CANDIDAT", "CANDIDAT")

                        // ── Vidéo progression ─────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/video-progression/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/video-progression/**").permitAll()
                        .requestMatchers("/api/chatbot/**").permitAll()

                        .anyRequest().authenticated()

                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost😘"));
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}