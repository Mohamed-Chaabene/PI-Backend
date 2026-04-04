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

    //  AuthenticationManager (login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    //  Password encoder (OBLIGATOIRE)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //  Configuration sécurité
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/domaines").permitAll()
                           .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/formations").permitAll()
                           .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/formations/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/entretiens/public/tests").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/questions/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/candidats/email/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/offres-emploi/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/questions/entretien/**").hasAnyAuthority("ROLE_RECRUTEUR", "ROLE_ADMIN")
                        .requestMatchers("/api/users/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/candidat/**").hasAuthority("ROLE_CANDIDAT")
                        .requestMatchers("/api/recruteur/**").hasAuthority("ROLE_RECRUTEUR")
                        .requestMatchers("/api/recruteurs/**").hasAuthority("ROLE_RECRUTEUR")
                        .requestMatchers("/api/client-freelance/**").hasAuthority("ROLE_CLIENT_FREELANCE")
                        // evenements
                        .requestMatchers(HttpMethod.DELETE, "/api/evenements/admin/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        //  Organisateur APRÈS
                        .requestMatchers(HttpMethod.DELETE, "/api/evenements/**").hasAuthority("ROLE_ORGANISATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/evenements").hasAuthority("ROLE_ORGANISATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/evenements/**").hasAuthority("ROLE_ORGANISATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/feedbacks").hasAuthority("ROLE_CANDIDAT")
                        .requestMatchers(HttpMethod.GET, "/api/feedbacks/**").hasAnyAuthority("ROLE_ORGANISATEUR", "ROLE_CANDIDAT")
                        .requestMatchers("/api/partenaires/**").permitAll()
                        .requestMatchers("/api/offres-partenaires/**").permitAll()
                        .anyRequest().authenticated()
                )

                //  IMPORTANT → chain fluide
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //  CORS configuration
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*")); // Allow localhost with any port
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}