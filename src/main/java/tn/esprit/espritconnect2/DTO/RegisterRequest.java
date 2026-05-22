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
import tn.esprit.espritconnect2.Entitie.Role;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    @NotBlank(message = "Le nom est requis")
    private String nom;

    @Email(message = "Format email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Minimum 6 caractères")
    private String password;

    @NotNull(message = "Le type d'utilisateur est requis")
    private Role role;

    private Niveau niveau;

    private String filiere;

    private String diplome;
    private String photo;
    
    // Champs Alumni
    private Integer anneePromotion;
    private String domaine;
    private Boolean disponibleMentorat;
    private String entrepriseActuelle;
    
    // Champs Entreprise (noms utilisés par le frontend)
    private String registreCommerce;
    private String secteurActivite;
    private String siteWeb;
    private String descriptionEntreprise;
    private String documentJustificatif;
}
