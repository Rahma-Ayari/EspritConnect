package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.AuthResponse;
import tn.esprit.espritconnect2.DTO.LoginRequest;
import tn.esprit.espritconnect2.DTO.RegisterRequest;
import tn.esprit.espritconnect2.Entitie.Alumni;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.AlumniRepository;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.security.JwtUtils;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final AlumniRepository alumniRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final ApprovalSettingsService approvalSettingsService;
    private final IEmailService emailService;

    @Override
    public AuthResponse login(LoginRequest req) {
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

            return AuthResponse.builder()
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

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà.");
        }
        
        Role role = req.getRole();
        if (role == null) {
            role = Role.ETUDIANT;
        }
        
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("Création de compte administrateur non autorisée.");
        }

        String encodedPassword = passwordEncoder.encode(req.getPassword());

        boolean shouldBeAutoApproved = approvalSettingsService.shouldAutoApprove(req.getEmail());
        
        User user = User.builder()
                .nom(req.getNom())
                .email(req.getEmail())
                .password(encodedPassword)
                .role(role)
                .enabled(false) // Toujours false à la création pour la base de données
                .status(Status.EN_ATTENTE) // On force aussi la colonne physique 'status' à EN_ATTENTE
                .build();
        
        log.info("Création de l'utilisateur {} - statut DB forcé à PENDING", user.getEmail());
        userRepository.save(user);

        if (!shouldBeAutoApproved) {
            log.info("Notification admin envoyée pour l'utilisateur pending: {}", user.getEmail());
            emailService.sendNewRegistrationNotification(user);
        }

        int scoreReadiness = 0;
        
        switch (role) {
            case ETUDIANT:
                if (etudiantRepository.existsByEmail(req.getEmail())) {
                    throw new IllegalArgumentException("Un étudiant avec cet email existe déjà.");
                }
                Etudiant etudiant = new Etudiant();
                etudiant.setNom(req.getNom());
                etudiant.setEmail(req.getEmail());
                etudiant.setPassword(encodedPassword);
                etudiant.setNiveau(req.getNiveau());
                etudiant.setFiliere(req.getFiliere());
                etudiant.setScoreReadiness(0);
                etudiant.setDateInscription(new Date());
                etudiantRepository.save(etudiant);
                break;
                
            case ALUMNI:
                if (alumniRepository.existsByEmail(req.getEmail())) {
                    throw new IllegalArgumentException("Un alumni avec cet email existe déjà.");
                }
                Alumni alumni = new Alumni();
                alumni.setNom(req.getNom());
                alumni.setEmail(req.getEmail());
                alumni.setPassword(encodedPassword);
                alumni.setAnneePromotion(req.getAnneePromotion());
                alumni.setDomaine(req.getDomaine());
                alumni.setDisponibleMentorat(req.getDisponibleMentorat() != null ? req.getDisponibleMentorat() : false);
                alumni.setEntrepriseActuelle(req.getEntrepriseActuelle());
                alumniRepository.save(alumni);
                break;
                
            case ENTREPRISE:
                break;
                
            case ENSEIGNANT:
                break;
                
            default:
                break;
        }

        String token = jwtUtils.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .role(role.name())
                .nom(req.getNom())
                .email(req.getEmail())
                .scoreReadiness(scoreReadiness)
                .userId(user.getId().toString())
                .build();
    }
}
