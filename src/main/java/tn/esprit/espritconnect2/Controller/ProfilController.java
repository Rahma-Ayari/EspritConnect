package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.ProfilRequestDTO;
import tn.esprit.espritconnect2.DTO.ProfilResponseDTO;
import tn.esprit.espritconnect2.Service.IProfilService;

import java.util.List;

/**
 * Controller REST pour la gestion des Profils.
 *
 * @RestController  → retourne automatiquement du JSON (pas besoin de @ResponseBody partout)
 * @RequestMapping  → toutes les routes commencent par /api/profils
 * @CrossOrigin     → autorise Angular (port 4200) à appeler ce backend
 * @RequiredArgsConstructor → injection du service via constructeur
 */
@RestController
@RequestMapping("/api/profils")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class ProfilController {

    // Injection de l'INTERFACE → bonne pratique (couplage faible)
    private final IProfilService profilService;

    /**
     * POST /api/profils
     * Crée un nouveau profil.
     * Retourne 201 CREATED avec le profil créé.
     */
    @PostMapping
    @tn.esprit.espritconnect2.annotation.TrackActivity(action = "UPDATE_USER", entity = "Profil", description = "User updated their profile")
    public ResponseEntity<ProfilResponseDTO> creerProfil(
            @Valid @RequestBody ProfilRequestDTO dto,
            org.springframework.security.core.Authentication authentication) {
        if (authentication != null) {
            dto.setUserId(authentication.getName());
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(profilService.creerProfil(dto));
    }

    /**
     * GET /api/profils
     * Récupère tous les profils.
     * Retourne 200 OK avec la liste.
     */
    @GetMapping
    public ResponseEntity<List<ProfilResponseDTO>> getAllProfils() {
        return ResponseEntity.ok(profilService.getAllProfils());
    }

    /**
     * GET /api/profils/{id}
     * Récupère un profil par son ID technique.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfilResponseDTO> getProfilById(@PathVariable Long id) {
        return ResponseEntity.ok(profilService.getProfilById(id));
    }

    /**
     * GET /api/profils/user/{userId}
     * Récupère un profil par le userId métier.
     * Utile quand Angular connaît l'email/userId mais pas l'ID technique.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfilResponseDTO> getProfilByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(profilService.getProfilByUserId(userId));
    }

    /**
     * GET /api/profils/current
     * Récupère le profil de l'utilisateur connecté.
     * L'email est passé via le header Authorization (JWT token).
     */
    @GetMapping("/current")
    public ResponseEntity<ProfilResponseDTO> getCurrentUserProfile(
            org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = authentication.getName();
        return ResponseEntity.ok(profilService.getCurrentUserProfile(email));
    }

    /**
     * PUT /api/profils/{id}
     * Met à jour les champs simples d'un profil (bio, photo, liens).
     * Les relations (etudiant, alumni...) ne sont pas modifiables via cet endpoint.
     */
    @PutMapping("/{id}")
    @tn.esprit.espritconnect2.annotation.TrackActivity(action = "UPDATE_USER", entity = "Profil", description = "User updated their profile")
    public ResponseEntity<ProfilResponseDTO> updateProfil(
            @PathVariable Long id,
            @Valid @RequestBody ProfilRequestDTO dto,
            org.springframework.security.core.Authentication authentication) {
        if (authentication != null) {
            dto.setUserId(authentication.getName());
        }
        return ResponseEntity.ok(profilService.updateProfil(id, dto));
    }

    /**
     * DELETE /api/profils/{id}
     * Supprime un profil.
     * Retourne 204 NO CONTENT (pas de corps dans la réponse).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfil(@PathVariable Long id) {
        profilService.deleteProfil(id);
        return ResponseEntity.noContent().build();
    }
}