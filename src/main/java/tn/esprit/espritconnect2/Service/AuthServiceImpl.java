package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import tn.esprit.espritconnect2.DTO.AuthResponse;
import tn.esprit.espritconnect2.DTO.EnterpriseRegisterRequest;
import tn.esprit.espritconnect2.DTO.LoginRequest;
import tn.esprit.espritconnect2.DTO.RegisterRequest;
import tn.esprit.espritconnect2.DTO.RegisterResponse;
import tn.esprit.espritconnect2.DTO.TwoFactorVerificationRequest;
import tn.esprit.espritconnect2.exception.EmailNotVerifiedException;
import tn.esprit.espritconnect2.Entitie.Alumni;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.UserDevice;
import tn.esprit.espritconnect2.Entitie.VerificationStatus;
import tn.esprit.espritconnect2.Repository.AlumniRepository;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Repository.UserDeviceRepository;
import tn.esprit.espritconnect2.security.JwtUtils;
import tn.esprit.espritconnect2.security.MfaRateLimiter;
import tn.esprit.espritconnect2.security.UserAgentParser;
import tn.esprit.espritconnect2.Entitie.PasswordResetToken;
import tn.esprit.espritconnect2.Repository.PasswordResetTokenRepository;
import tn.esprit.espritconnect2.exception.AccountLockedException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

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
    private final IFileStorageService fileStorageService;
    private final UserDeviceRepository userDeviceRepository;
    private final LoginHistoryService loginHistoryService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final MfaRateLimiter mfaRateLimiter;
    private final UserAgentParser userAgentParser;
    private final HttpServletRequest request;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;
    
    @Value("${app.email-verification.expose-link-on-register:false}")
    private boolean exposeVerificationLinkOnRegister;

    @Value("${app.google.client-id:YOUR_GOOGLE_CLIENT_ID_HERE}")
    private String googleClientId;

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isAccountLocked()) {
                long secondsLocked = java.time.Duration.between(LocalDateTime.now(), user.getAccountLockedUntil()).getSeconds();
                if (secondsLocked > 0) {
                    throw new AccountLockedException("Votre compte est temporairement verrouillé suite à plusieurs tentatives de connexion échouées.", 0, secondsLocked);
                } else {
                    user.setFailedLoginAttempts(0);
                    user.setAccountLockedUntil(null);
                    userRepository.save(user);
                }
            }
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

            User user = (User) auth.getPrincipal();

            if (user.getFailedLoginAttempts() > 0 || user.getAccountLockedUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setAccountLockedUntil(null);
                userRepository.save(user);
            }

            // 2FA : utilisateurs uniquement (étudiant, alumni, enseignant — pas admin ni entreprise)
            boolean isMfaApplicable = user.isTwoFactorEnabled() && user.getRole().isMfaEligible();

            if (isMfaApplicable) {
                boolean deviceTrusted = false;
                if (req.getDeviceToken() != null && !req.getDeviceToken().trim().isEmpty()) {
                    Optional<UserDevice> deviceOpt = userDeviceRepository.findByDeviceTokenAndUser(req.getDeviceToken(), user);
                    if (deviceOpt.isPresent() && deviceOpt.get().getExpiresAt().isAfter(LocalDateTime.now())) {
                        deviceTrusted = true;
                    }
                }

                if (!deviceTrusted) {
                    loginHistoryService.recordLoginAttempt(user, getClientIp(request), request.getHeader("User-Agent"), "PENDING_2FA");
                    return AuthResponse.builder()
                            .mfaRequired(true)
                            .email(user.getEmail())
                            .mfaPendingToken(jwtUtils.generateMfaPendingToken(user.getEmail()))
                            .build();
                }
            }

            String token = jwtUtils.generateToken(user);

            int score = 0;
            if (user.getRole() == Role.ETUDIANT) {
                score = etudiantRepository.findByEmail(user.getEmail())
                        .map(e -> e.getScoreReadiness() != null ? e.getScoreReadiness() : 0)
                        .orElse(0);
            }

            loginHistoryService.recordLoginAttempt(user, getClientIp(request), request.getHeader("User-Agent"), "SUCCESS");

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
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);

                String ip = getClientIp(request);
                String userAgent = request.getHeader("User-Agent");
                String browserOs = "Inconnu";
                if (userAgent != null) {
                    try {
                        var parsedUA = userAgentParser.parse(userAgent);
                        browserOs = parsedUA.browser + " sur " + parsedUA.os;
                    } catch (Exception uaEx) {
                        browserOs = userAgent;
                    }
                }

                if (attempts == 3) {
                    emailService.sendSuspiciousLoginWarningEmail(user, ip, browserOs);
                } else if (attempts >= 5) {
                    user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
                    userRepository.save(user);
                    emailService.sendAccountLockoutEmail(user, ip, browserOs);
                    loginHistoryService.recordLoginAttempt(user, ip, userAgent, "FAILED_PASSWORD_LOCKOUT");
                    throw new AccountLockedException("Votre compte est temporairement verrouillé pour 15 minutes.", 0, 15 * 60);
                }

                userRepository.save(user);
                loginHistoryService.recordLoginAttempt(user, ip, userAgent, "FAILED_PASSWORD");
                int remaining = Math.max(0, 5 - attempts);
                throw new BadCredentialsException("Email ou mot de passe incorrect. Tentatives restantes : " + remaining);
            }
            throw new BadCredentialsException("Email ou mot de passe incorrect.");
        } catch (EmailNotVerifiedException e) {
            userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
                loginHistoryService.recordLoginAttempt(user, getClientIp(request), request.getHeader("User-Agent"), "FAILED_EMAIL_NOT_VERIFIED");
            });
            throw e;
        } catch (DisabledException e) {
            userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
                loginHistoryService.recordLoginAttempt(user, getClientIp(request), request.getHeader("User-Agent"), "FAILED_DISABLED");
            });
            throw new DisabledException("Votre compte est en attente de validation par l'administrateur.");
        }
    }

    @Override
    public AuthResponse verify2faLogin(TwoFactorVerificationRequest verifyReq, String ipAddress, String userAgent) {
        if (!jwtUtils.validateMfaPendingToken(verifyReq.getMfaPendingToken(), verifyReq.getEmail())) {
            throw new BadCredentialsException("Session 2FA expirée. Reconnectez-vous avec votre email et mot de passe.");
        }

        User user = userRepository.findByEmail(verifyReq.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect."));

        if (!user.getRole().isMfaEligible()) {
            throw new BadCredentialsException("L'authentification 2FA n'est pas applicable à ce type de compte.");
        }

        if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            throw new BadCredentialsException("La double authentification n'est pas activée sur ce compte.");
        }

        if (mfaRateLimiter.isLocked(user.getEmail())) {
            throw new BadCredentialsException("Trop de tentatives de code 2FA. Votre compte est bloqué pour 15 minutes.");
        }

        boolean isCodeValid = false;
        boolean isBackupUsed = false;
        String rawCode = verifyReq.getCode();

        if (twoFactorAuthService.isBackupCodeFormat(rawCode)) {
            String inputCode = twoFactorAuthService.normalizeBackupCode(rawCode);
            if (user.getBackupCodes() != null && user.getBackupCodes().contains(inputCode)) {
                user.getBackupCodes().remove(inputCode);
                userRepository.save(user);
                isCodeValid = true;
                isBackupUsed = true;
            }
        } else {
            isCodeValid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), rawCode);
        }

        if (!isCodeValid) {
            mfaRateLimiter.recordFailure(user.getEmail());
            loginHistoryService.recordLoginAttempt(user, ipAddress, userAgent, "FAILED_2FA");
            throw new BadCredentialsException("Code de double authentification incorrect. Restant: " + mfaRateLimiter.getRemainingAttempts(user.getEmail()));
        }

        mfaRateLimiter.recordSuccess(user.getEmail());

        String newDeviceToken = null;
        if (verifyReq.isRememberDevice()) {
            newDeviceToken = UUID.randomUUID().toString();
            UserDevice userDevice = UserDevice.builder()
                    .user(user)
                    .deviceToken(newDeviceToken)
                    .deviceName(userAgentParser.parse(userAgent).browser + " on " + userAgentParser.parse(userAgent).os)
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .build();
            userDeviceRepository.save(userDevice);
        }

        loginHistoryService.recordLoginAttempt(user, ipAddress, userAgent, isBackupUsed ? "SUCCESS_BACKUP" : "SUCCESS");

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
                .deviceToken(newDeviceToken)
                .build();
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest req) {
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
        boolean emailVerificationRequired = approvalSettingsService.getSettings().isRequireEmailVerification();

        User user = User.builder()
                .nom(req.getNom())
                .email(req.getEmail())
                .password(encodedPassword)
                .role(role)
                .enabled(false)
                .emailVerified(false)
                .status(Status.EN_ATTENTE)
                .build();
        
        log.info("Création de l'utilisateur {} - statut DB forcé à PENDING", user.getEmail());
        userRepository.save(user);

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
                // Enregistrer les données spécifiques à l'entreprise
                if (req.getRegistreCommerce() != null && !req.getRegistreCommerce().trim().isEmpty()) {
                    user.setBusinessRegistrationNumber(req.getRegistreCommerce().trim());
                }
                if (req.getSecteurActivite() != null && !req.getSecteurActivite().trim().isEmpty()) {
                    user.setCompanySector(req.getSecteurActivite().trim());
                }
                if (req.getSiteWeb() != null && !req.getSiteWeb().trim().isEmpty()) {
                    user.setCompanyWebsite(req.getSiteWeb().trim());
                }
                if (req.getDescriptionEntreprise() != null && !req.getDescriptionEntreprise().trim().isEmpty()) {
                    user.setCompanyDescription(req.getDescriptionEntreprise().trim());
                }
                
                // Traiter le document justificatif si fourni (base64)
                if (req.getDocumentJustificatif() != null && !req.getDocumentJustificatif().trim().isEmpty()) {
                    try {
                        String documentPath = fileStorageService.storeBase64Document(
                            req.getDocumentJustificatif(), 
                            user.getId().toString()
                        );
                        user.setVerificationDocumentPath(documentPath);
                        user.setVerificationDocumentName("document_justificatif");
                        user.setVerificationStatus(VerificationStatus.PENDING);
                        log.info("Document justificatif enregistré pour l'entreprise {}", user.getEmail());
                    } catch (Exception e) {
                        log.warn("Erreur lors de l'enregistrement du document pour {}: {}", user.getEmail(), e.getMessage());
                        user.setVerificationStatus(VerificationStatus.NOT_SUBMITTED);
                    }
                } else {
                    user.setVerificationStatus(VerificationStatus.NOT_SUBMITTED);
                }
                
                userRepository.save(user);
                log.info("Entreprise {} inscrite", user.getEmail());
                break;
                
            case ENSEIGNANT:
                break;
                
            default:
                break;
        }

        boolean autoApproved = false;
        if (shouldBeAutoApproved && !emailVerificationRequired) {
            autoApproved = approvalSettingsService.applyAutoApproval(user);
            if (autoApproved) {
                user.setEmailVerified(true);
                userRepository.save(user);
                emailService.sendApprovalNotification(user);
            }
        }

        if (!autoApproved) {
            log.info("Notification admin envoyée pour l'utilisateur pending: {}", user.getEmail());
            emailService.sendNewRegistrationNotification(user);
        }

        String verificationUrl = null;
        if (emailVerificationRequired || !autoApproved) {
            verificationUrl = emailVerificationService.sendVerificationEmail(user);
        }

        return RegisterResponse.builder()
                .message(autoApproved
                        ? "Votre compte a été approuvé automatiquement."
                        : "Veuillez vérifier votre email.")
                .email(req.getEmail())
                .emailVerificationRequired(emailVerificationRequired && !autoApproved)
                .verificationUrl(exposeVerificationLinkOnRegister ? verificationUrl : null)
                .build();
    }

    @Override
    @Transactional
    public RegisterResponse registerEnterprise(EnterpriseRegisterRequest req, MultipartFile document) {
        if (document == null || document.isEmpty()) {
            throw new IllegalArgumentException("Le document justificatif est obligatoire pour l'inscription d'une entreprise.");
        }

        if (!fileStorageService.isValidDocumentType(document)) {
            throw new IllegalArgumentException("Type de document non valide. Formats acceptés: PDF, JPG, JPEG, PNG.");
        }

        if (!fileStorageService.isValidDocumentSize(document)) {
            throw new IllegalArgumentException("Le document est trop volumineux. Taille maximale: 10 MB.");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà.");
        }

        String encodedPassword = passwordEncoder.encode(req.getPassword());
        boolean shouldBeAutoApproved = approvalSettingsService.shouldAutoApprove(req.getEmail());
        boolean emailVerificationRequired = approvalSettingsService.getSettings().isRequireEmailVerification();

        User user = User.builder()
                .nom(req.getNom())
                .email(req.getEmail())
                .password(encodedPassword)
                .role(Role.ENTREPRISE)
                .enabled(false)
                .emailVerified(false)
                .status(Status.EN_ATTENTE)
                .businessRegistrationNumber(req.getBusinessRegistrationNumber())
                .companySector(req.getCompanySector())
                .companyWebsite(req.getCompanyWebsite())
                .companyDescription(req.getCompanyDescription())
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        userRepository.save(user);

        String documentPath = fileStorageService.storeVerificationDocument(document, user.getId().toString());
        user.setVerificationDocumentPath(documentPath);
        user.setVerificationDocumentName(document.getOriginalFilename());
        userRepository.save(user);

        log.info("Entreprise {} inscrite avec document justificatif: {}", user.getEmail(), document.getOriginalFilename());

        boolean autoApproved = false;
        if (shouldBeAutoApproved && !emailVerificationRequired) {
            autoApproved = approvalSettingsService.applyAutoApproval(user);
            if (autoApproved) {
                user.setEmailVerified(true);
                userRepository.save(user);
                emailService.sendApprovalNotification(user);
            }
        }

        if (!autoApproved) {
            log.info("Notification admin envoyée pour l'entreprise pending: {}", user.getEmail());
            emailService.sendNewRegistrationNotification(user);
        }

        if (emailVerificationRequired || !autoApproved) {
            emailVerificationService.sendVerificationEmail(user);
        }

        return RegisterResponse.builder()
                .message(autoApproved
                        ? "Votre compte a été approuvé automatiquement."
                        : "Veuillez vérifier votre email.")
                .email(req.getEmail())
                .emailVerificationRequired(emailVerificationRequired && !autoApproved)
                .build();
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return; // Fail silently for security
        }
        User user = userOpt.get();

        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        
        passwordResetTokenRepository.save(resetToken);

        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user, resetUrl);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Jeton invalide."));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Jeton expiré.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                Optional<User> userOpt = userRepository.findByEmail(email);
                User user;
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                    if (!user.isEnabled()) {
                        throw new DisabledException("Votre compte est en attente de validation par l'administrateur.");
                    }
                } else {
                    user = User.builder()
                            .nom(name)
                            .email(email)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .role(Role.ETUDIANT)
                            .enabled(true)
                            .emailVerified(true)
                            .status(Status.ACCEPTEE)
                            .build();
                    userRepository.save(user);

                    Etudiant etudiant = new Etudiant();
                    etudiant.setNom(name);
                    etudiant.setEmail(email);
                    etudiant.setPassword(user.getPassword());
                    etudiant.setScoreReadiness(0);
                    etudiant.setDateInscription(new Date());
                    etudiantRepository.save(etudiant);
                }

                loginHistoryService.recordLoginAttempt(user, getClientIp(request), request.getHeader("User-Agent"), "SUCCESS_GOOGLE");

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

            } else {
                throw new BadCredentialsException("Token Google invalide.");
            }
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du token Google", e);
            throw new BadCredentialsException("Échec de l'authentification Google.");
        }
    }
}
