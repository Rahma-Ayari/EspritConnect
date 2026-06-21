package tn.esprit.espritconnect2.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import tn.esprit.espritconnect2.Config.ApiOfficePaths;



import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(HttpMethod.GET, "/", "/error").permitAll()
                .requestMatchers("/uploads/**").permitAll()

                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/google-login",
                        "/api/auth/verify-2fa-login",
                        "/api/auth/register-enterprise",
                        "/api/auth/verify-email",
                        "/api/auth/resend-verification-email",
                        "/api/auth/forgot-password",
                        "/api/auth/reset-password",
                        "/api/captcha/**"
                ).permitAll()
                .requestMatchers("/api/auth/**").authenticated()
                .requestMatchers("/api/offres/public/**").permitAll()

                .requestMatchers("/api/evenements/upcoming").permitAll()
                .requestMatchers("/api/admin/dashboard/**").permitAll()
                .requestMatchers("/api/admin/settings/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/registration/settings").permitAll()
                // DEV: open job APIs while building entreprise job dashboard (tighten before prod)
                .requestMatchers("/api/offres/**").permitAll()
                .requestMatchers("/api/ai/**").permitAll()
                .requestMatchers("/api/matchings/**").permitAll()
                .requestMatchers("/api/candidatures/**").permitAll()
                .requestMatchers("/api/etudiants/me", "/api/alumni/me").authenticated()
                .requestMatchers("/api/entreprises/*/job-dashboard").permitAll()
                .requestMatchers("/api/entreprises/*/verification/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/entreprises/*").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Public self-registration: create company without JWT (pending admin approval).
                .requestMatchers(HttpMethod.POST, "/api/entreprises").permitAll()
                .requestMatchers("/api/entreprises/**").hasAnyRole("ENTREPRISE", "ADMIN")

                // Forum & Email Communications (back-office & front-office)
                .requestMatchers("/api/forum/**").permitAll()
                .requestMatchers("/api/forum-groups/**").permitAll()
                .requestMatchers("/api/email-communications/**").permitAll()

                // Activity Digest & Config
                .requestMatchers("/api/digest-config/**").permitAll()
                .requestMatchers("/api/activity-digest/**").permitAll()

                // Support, Badges, Moderation
                .requestMatchers("/api/badges/**").permitAll()
                .requestMatchers("/api/support/**").permitAll()
                .requestMatchers("/api/moderation/**").permitAll()

                .anyRequest().authenticated()

            )

            .authenticationProvider(authenticationProvider)

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
