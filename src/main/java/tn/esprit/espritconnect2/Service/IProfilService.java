package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.ProfilRequestDTO;
import tn.esprit.espritconnect2.DTO.ProfilResponseDTO;

import java.util.List;

/**
 * Interface du service Profil.
 * Définit le contrat des opérations disponibles.
 * Le controller dépend de cette interface, pas de l'implémentation → couplage faible.
 */
public interface IProfilService {

    // Créer un nouveau profil
    ProfilResponseDTO creerProfil(ProfilRequestDTO dto);

    // Récupérer tous les profils
    List<ProfilResponseDTO> getAllProfils();

    // Récupérer un profil par son ID
    ProfilResponseDTO getProfilById(Long id);

    // Récupérer un profil par userId
    ProfilResponseDTO getProfilByUserId(String userId);

    // Récupérer le profil de l'utilisateur connecté
    ProfilResponseDTO getCurrentUserProfile(String email);

    // Modifier un profil existant
    ProfilResponseDTO updateProfil(Long id, ProfilRequestDTO dto);

    // Supprimer un profil
    void deleteProfil(Long id);
}