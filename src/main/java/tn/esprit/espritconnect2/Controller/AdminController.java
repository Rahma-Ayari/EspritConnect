package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.AdminRequestDTO;
import tn.esprit.espritconnect2.DTO.AdminResponseDTO;
import tn.esprit.espritconnect2.Service.IAdminService;

import java.util.List;

/**
 * Controller REST pour la gestion des Administrateurs.
 *
 * @RestController  → indique que cette classe est un contrôleur REST (retourne du JSON)
 * @RequestMapping  → préfixe commun pour toutes les routes de ce contrôleur
 * @CrossOrigin     → autorise les requêtes depuis Angular (localhost:4200 par défaut)
 * @RequiredArgsConstructor → injection du service via constructeur (bonne pratique)
 */
@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "http://localhost:4200") // À ajuster selon ton port Angular
@RequiredArgsConstructor
public class AdminController {

    // On injecte l'INTERFACE, pas l'implémentation → couplage faible
    private final IAdminService adminService;

    /**
     * POST /api/admins
     * Crée un nouvel administrateur.
     * @Valid → active la validation du DTO (annotations @NotBlank, @Email, etc.)
     * Retourne 201 CREATED avec le DTO de l'admin créé.
     */
    @PostMapping
    public ResponseEntity<AdminResponseDTO> creerAdmin(
            @Valid @RequestBody AdminRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.ajouterAdmin(dto));
    }

    /**
     * GET /api/admins
     * Récupère la liste de tous les administrateurs.
     * Retourne 200 OK avec la liste des DTOs.
     */
    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    /**
     * GET /api/admins/{id}
     * Récupère un administrateur par son ID.
     * @PathVariable → extrait l'ID depuis l'URL.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> getAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    /**
     * PUT /api/admins/{id}
     * Met à jour les informations d'un administrateur existant.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminRequestDTO dto) {
        return ResponseEntity.ok(adminService.updateAdmin(id, dto));
    }

    /**
     * DELETE /api/admins/{id}
     * Supprime un administrateur par son ID.
     * Retourne 204 NO CONTENT (pas de corps dans la réponse).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
}