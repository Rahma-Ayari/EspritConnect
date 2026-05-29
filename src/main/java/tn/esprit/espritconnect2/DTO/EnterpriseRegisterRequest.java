package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnterpriseRegisterRequest {
    
    @NotBlank(message = "Le nom de l'entreprise est requis")
    private String nom;

    @Email(message = "Format email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Minimum 6 caractères")
    private String password;

    @NotBlank(message = "Le numéro d'enregistrement commercial (RC/MF) est requis")
    private String businessRegistrationNumber;

    private String companySector;

    private String companyWebsite;

    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
    private String companyDescription;
}
