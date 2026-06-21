package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import tn.esprit.espritconnect2.DTO.*;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.LoginHistory;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Service.AuthServiceImpl;
import tn.esprit.espritconnect2.Service.EmailVerificationService;
import tn.esprit.espritconnect2.Service.TwoFactorAuthService;
import tn.esprit.espritconnect2.Service.LoginHistoryService;
import tn.esprit.espritconnect2.exception.EmailNotVerifiedException;
import tn.esprit.espritconnect2.exception.EmailVerificationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import tn.esprit.espritconnect2.Repository.UserDeviceRepository;
import tn.esprit.espritconnect2.exception.AccountLockedException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;
    private final EmailVerificationService emailVerificationService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final LoginHistoryService loginHistoryService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDeviceRepository userDeviceRepository;

    private User reloadUser(org.springframework.security.core.Authentication auth) {
        User principal = (User) auth.getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            AuthResponse response = authService.login(req);
            return ResponseEntity.ok(response);
        } catch (AccountLockedException e) {
            return ResponseEntity.status(423) // Locked
                    .body(Map.of(
                            "message", e.getMessage(),
                            "code", "ACCOUNT_LOCKED",
                            "remainingAttempts", e.getRemainingAttempts(),
                            "lockoutSeconds", e.getLockoutSeconds()
                    ));
        } catch (BadCredentialsException e) {
            int remainingAttempts = 5;
            String msg = e.getMessage();
            if (msg.contains("Tentatives restantes :")) {
                try {
                    String parts[] = msg.split("Tentatives restantes : ");
                    remainingAttempts = Integer.parseInt(parts[1].trim());
                } catch (Exception ex) {
                    // fallback
                }
            }
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "message", msg,
                            "remainingAttempts", remainingAttempts
                    ));
        } catch (EmailNotVerifiedException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", e.getMessage(), "code", "EMAIL_NOT_VERIFIED"));
        } catch (org.springframework.security.authentication.DisabledException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest req) {
        try {
            AuthResponse response = authService.googleLogin(req.getIdToken());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException | org.springframework.security.authentication.DisabledException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            RegisterResponse response = authService.register(req);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            emailVerificationService.verifyEmail(token);
            return ResponseEntity.ok(Map.of(
                    "message", "Votre compte a été vérifié.",
                    "success", true
            ));
        } catch (EmailVerificationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "code", e.getErrorCode(),
                    "success", false
            ));
        }
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "L'email est requis."));
        }
        try {
            emailVerificationService.resendVerificationEmail(email);
            return ResponseEntity.ok(Map.of("message", "Un nouvel email de vérification a été envoyé."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(org.springframework.security.core.Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("email", auth.getName(),
                                        "roles", auth.getAuthorities()));
    }

    @PostMapping(value = "/register-enterprise", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerEnterprise(
            @RequestParam("nom") String nom,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("businessRegistrationNumber") String businessRegistrationNumber,
            @RequestParam(value = "companySector", required = false) String companySector,
            @RequestParam(value = "companyWebsite", required = false) String companyWebsite,
            @RequestParam(value = "companyDescription", required = false) String companyDescription,
            @RequestPart("document") MultipartFile document) {
        try {
            EnterpriseRegisterRequest req = EnterpriseRegisterRequest.builder()
                    .nom(nom)
                    .email(email)
                    .password(password)
                    .businessRegistrationNumber(businessRegistrationNumber)
                    .companySector(companySector)
                    .companyWebsite(companyWebsite)
                    .companyDescription(companyDescription)
                    .build();

            RegisterResponse response = authService.registerEnterprise(req, document);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify-2fa-login")
    public ResponseEntity<?> verify2faLogin(@Valid @RequestBody TwoFactorVerificationRequest req, HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            } else {
                ipAddress = ipAddress.split(",")[0].trim();
            }
            AuthResponse response = authService.verify2faLogin(req, ipAddress, userAgent);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    private static final String MFA_NOT_ELIGIBLE_MSG =
            "2FA indisponible pour les comptes administrateur et entreprise.";

    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setup2fa(org.springframework.security.core.Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = reloadUser(auth);
        if (!user.getRole().isMfaEligible()) {
            return ResponseEntity.badRequest().body(Map.of("message", MFA_NOT_ELIGIBLE_MSG));
        }
        try {
            String secret = twoFactorAuthService.generateNewSecret();
            user.setTwoFactorSecret(secret);
            userRepository.save(user);

            String qrCode = twoFactorAuthService.generateQrCodeImageUri(user.getEmail(), secret);
            return ResponseEntity.ok(MfaSetupResponse.builder().secret(secret).qrCode(qrCode).build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur lors de la génération du QR Code."));
        }
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verify2fa(org.springframework.security.core.Authentication auth, @RequestBody Map<String, String> body) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = reloadUser(auth);
        if (!user.getRole().isMfaEligible()) {
            return ResponseEntity.badRequest().body(Map.of("message", MFA_NOT_ELIGIBLE_MSG));
        }
        String code = body.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Code TOTP requis"));
        }
        if (user.getTwoFactorSecret() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lancez d'abord la configuration 2FA (scan du QR code)."));
        }

        boolean isValid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), code);
        if (!isValid) {
            return ResponseEntity.badRequest().body(Map.of("message", "Code TOTP incorrect"));
        }

        user.setTwoFactorEnabled(true);
        List<String> backupCodes = twoFactorAuthService.generateBackupCodes();
        user.setBackupCodes(backupCodes);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "backupCodes", backupCodes));
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disable2fa(org.springframework.security.core.Authentication auth, @Valid @RequestBody TwoFactorDisableRequest req) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = reloadUser(auth);
        if (!user.getRole().isMfaEligible()) {
            return ResponseEntity.badRequest().body(Map.of("message", MFA_NOT_ELIGIBLE_MSG));
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mot de passe actuel incorrect"));
        }

        boolean isValid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), req.getCode());
        if (!isValid) {
            return ResponseEntity.badRequest().body(Map.of("message", "Code TOTP incorrect"));
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        if (user.getBackupCodes() != null) {
            user.getBackupCodes().clear();
        }
        userRepository.save(user);
        userDeviceRepository.deleteByUser(user);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/2fa/regenerate-backup-codes")
    public ResponseEntity<?> regenerateBackupCodes(org.springframework.security.core.Authentication auth, @Valid @RequestBody TwoFactorDisableRequest req) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = reloadUser(auth);
        if (!user.getRole().isMfaEligible()) {
            return ResponseEntity.badRequest().body(Map.of("message", MFA_NOT_ELIGIBLE_MSG));
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mot de passe actuel incorrect"));
        }

        boolean isValid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), req.getCode());
        if (!isValid) {
            return ResponseEntity.badRequest().body(Map.of("message", "Code TOTP incorrect"));
        }

        List<String> backupCodes = twoFactorAuthService.generateBackupCodes();
        user.setBackupCodes(backupCodes);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "backupCodes", backupCodes));
    }

    @GetMapping("/2fa/status")
    public ResponseEntity<?> get2faStatus(org.springframework.security.core.Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = reloadUser(auth);
        int backupCount = user.getBackupCodes() != null ? user.getBackupCodes().size() : 0;
        return ResponseEntity.ok(Map.of(
                "twoFactorEnabled", user.isTwoFactorEnabled(),
                "backupCodesCount", backupCount,
                "mfaApplicable", user.getRole().isMfaEligible()
        ));
    }

    @GetMapping("/2fa/login-history")
    public ResponseEntity<?> getLoginHistory(org.springframework.security.core.Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = reloadUser(auth);
        List<LoginHistory> history = loginHistoryService.getLoginHistory(user);
        List<Map<String, Object>> response = history.stream().map(h -> Map.<String, Object>of(
                "id", h.getId(),
                "loginTime", h.getLoginTime(),
                "ipAddress", h.getIpAddress(),
                "os", h.getOs(),
                "browser", h.getBrowser(),
                "device", h.getDevice(),
                "location", h.getLocation(),
                "status", h.getStatus()
        )).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest body) {
        authService.requestPasswordReset(body.getEmail(), body.getCaptchaId(), body.getCaptchaToken());
        return ResponseEntity.ok(Map.of("message", "Si cet email correspond à un compte existant, un lien de réinitialisation vous a été envoyé."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le jeton et le nouveau mot de passe sont requis."));
        }

        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Votre mot de passe a été réinitialisé avec succès."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
