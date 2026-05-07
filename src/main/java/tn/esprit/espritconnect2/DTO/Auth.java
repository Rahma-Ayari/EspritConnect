package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.espritconnect2.Entitie.Niveau;

public class Auth {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginRequest {
        @Email(message = "Format email invalide")
        @NotBlank(message = "L'email est requis")
        private String email;

        @NotBlank(message = "Le mot de passe est requis")
        @Size(min = 6, message = "Minimum 6 caractères")
        private String password;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Le nom est requis")
        private String nom;

        @Email(message = "Format email invalide")
        @NotBlank(message = "L'email est requis")
        private String email;

        @NotBlank(message = "Le mot de passe est requis")
        @Size(min = 6, message = "Minimum 6 caractères")
        private String password;

        @NotNull(message = "Le niveau est requis")
        private Niveau niveau;

        @NotBlank(message = "La filière est requise")
        private String filiere;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuthResponse {
        private String token;
        private String type = "Bearer";
        private String role;
        private String nom;
        private String email;
        private int    scoreReadiness;
        private String userId;
    }
}
