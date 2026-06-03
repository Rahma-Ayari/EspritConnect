package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO reçu depuis le client lors de la création ou modification d'un profil.
 * On n'expose pas les relations (etudiant, alumni, etc.) ici → trop complexe et inutile.
 * Le client envoie uniquement les champs simples du profil.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfilRequestDTO {

    // Identifiant métier lié à l'utilisateur (ex: email ou ID externe)
    private String userId;

    // Lien vers la photo de profil (URL ou chemin)
    private String photo;

    // Lien LinkedIn de l'utilisateur
    private String lienLinkedIn;

    // Biographie courte de l'utilisateur
    @Size(max = 500, message = "La bio ne doit pas dépasser 500 caractères")
    private String bio;

    // Lien GitHub de l'utilisateur
    private String lienGitHub;

    // Champs additionnels pour un profil complet
    private String prenom;
    private String telephone;
    private String adresse;
    private String ville;
    private String pays;
    private String codePostal;
    private String siteWeb;
    private String dateNaissance;
    private String genre;
    private String nomProprietaire;
}