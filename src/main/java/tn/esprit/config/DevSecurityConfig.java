package tn.esprit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Development‑only security configuration that disables authentication and CORS restrictions.
 * Activate it by running the application with the `dev` profile (e.g. `-Dspring.profiles.active=dev`).
 * This allows the Angular front‑end to call the back‑end endpoints without receiving a 403.
 */
@Configuration
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Permit all requests without authentication
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            // Disable CSRF for simplicity in dev
            .csrf(csrf -> csrf.disable())
            // Allow CORS from any origin (helpful when Angular runs on localhost:4200)
            .cors(Customizer.withDefaults());
        return http.build();
    }
}
