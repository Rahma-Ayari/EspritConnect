package tn.esprit.espritconnect2.DTO;

import lombok.*;

/**
 * DTO renvoyé au client après lecture ou modification d'un profil.
 * On inclut des infos de base sur le propriétaire du profil (nom + type)
 * pour que Angular sache à qui appartient ce profil, sans charger toute l'entité.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilResponseDTO {

    private Long idProfil;

    private String userId;
    private String photo;
    private String lienLinkedIn;
    private String bio;
    private String lienGitHub;

    // Nom du propriétaire du profil (Etudiant, Alumni, Admin, ou Entreprise)
    // Calculé dans le service selon qui est lié à ce profil
    private String nomProprietaire;

    // Type du propriétaire : "ETUDIANT", "ALUMNI", "ENTREPRISE", "ADMINISTRATEUR"
    private String typeProprietaire;
}