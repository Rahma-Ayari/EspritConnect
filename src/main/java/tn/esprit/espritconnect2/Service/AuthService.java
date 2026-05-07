package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.Auth;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.security.JwtUtils;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public Auth.AuthResponse login(Auth.LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

            User user = (User) auth.getPrincipal();
            String token = jwtUtils.generateToken(user);

            int score = 0;
            if (user.getRole() == Role.ETUDIANT) {
                score = etudiantRepository.findByEmail(user.getEmail())
                        .map(e -> e.getScoreReadiness() != null ? e.getScoreReadiness() : 0)
                        .orElse(0);
            }

            return Auth.AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .role(user.getRole().name())
                    .nom(user.getNom())
                    .email(user.getEmail())
                    .scoreReadiness(score)
                    .userId(user.getId().toString())
                    .build();

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Email ou mot de passe incorrect.");
        } catch (DisabledException e) {
            throw new DisabledException("Votre compte est en attente de validation par l'administrateur.");
        }
    }

    @Transactional
    public Auth.AuthResponse register(Auth.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())
                || etudiantRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà.");
        }

        String encodedPassword = passwordEncoder.encode(req.getPassword());

        User user = User.builder()
                .nom(req.getNom())
                .email(req.getEmail())
                .password(encodedPassword)
                .role(Role.ETUDIANT)
                .enabled(false) // En attente de validation admin
                .build();
        userRepository.save(user);

        Etudiant etudiant = new Etudiant();
        etudiant.setNom(req.getNom());
        etudiant.setEmail(req.getEmail());
        etudiant.setPassword(encodedPassword);
        etudiant.setNiveau(req.getNiveau());
        etudiant.setFiliere(req.getFiliere());
        etudiant.setScoreReadiness(0);
        etudiant.setDateInscription(new Date());
        etudiantRepository.save(etudiant);

        String token = jwtUtils.generateToken(user);

        return Auth.AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .role(Role.ETUDIANT.name())
                .nom(etudiant.getNom())
                .email(etudiant.getEmail())
                .scoreReadiness(0)
                .userId(user.getId().toString())
                .build();
    }
}
