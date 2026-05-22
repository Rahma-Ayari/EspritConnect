package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.AuthResponse;
import tn.esprit.espritconnect2.DTO.EnterpriseRegisterRequest;
import tn.esprit.espritconnect2.DTO.LoginRequest;
import tn.esprit.espritconnect2.DTO.RegisterRequest;
import tn.esprit.espritconnect2.Service.AuthServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            AuthResponse response = authService.login(req);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", e.getMessage()));
        } catch (org.springframework.security.authentication.DisabledException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            AuthResponse response = authService.register(req);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
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

            AuthResponse response = authService.registerEnterprise(req, document);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
