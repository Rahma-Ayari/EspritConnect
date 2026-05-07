package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.AdminRequestDTO;
import tn.esprit.espritconnect2.DTO.AdminResponseDTO;

import java.util.List;

/**
 * Interface du service Admin.
 * Bonne pratique : toujours définir une interface avant l'implémentation.
 * Cela permet de changer l'implémentation sans toucher aux autres couches.
 */
public interface IAdminService {

    // Créer un nouvel administrateur
    AdminResponseDTO ajouterAdmin(AdminRequestDTO dto);

    // Récupérer tous les admins
    List<AdminResponseDTO> getAllAdmins();

    // Récupérer un admin par son ID
    AdminResponseDTO getAdminById(Long id);

    // Modifier un admin existant
    AdminResponseDTO updateAdmin(Long id, AdminRequestDTO dto);

    // Supprimer un admin par son ID
    void deleteAdmin(Long id);
}