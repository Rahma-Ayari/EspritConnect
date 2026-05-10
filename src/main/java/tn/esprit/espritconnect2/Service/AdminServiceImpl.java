package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.AdminRequestDTO;
import tn.esprit.espritconnect2.DTO.AdminResponseDTO;
import tn.esprit.espritconnect2.Entitie.Administrateur;
import tn.esprit.espritconnect2.Repository.AdministrateurRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service Admin.
 * @Service → Spring gère cette classe comme un bean de la couche service.
 * @RequiredArgsConstructor → génère un constructeur avec les champs final (injection de dépendances).
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final AdministrateurRepository adminRepository;

    // ─── Mapper DTO → Entité ──────────────────────────────────────────────────
    /**
     * Convertit un AdminRequestDTO (données reçues du client) en entité Administrateur.
     * Utilisé lors de la création et la modification.
     */
    private Administrateur toEntity(AdminRequestDTO dto) {
        Administrateur admin = new Administrateur();
        admin.setNom(dto.getNom());
        admin.setEmail(dto.getEmail());
        admin.setPassword(dto.getPassword()); // ⚠️ À encoder avec BCrypt quand Spring Security sera intégré
        admin.setRole(dto.getRole());
        return admin;
    }

    // ─── Mapper Entité → ResponseDTO ─────────────────────────────────────────
    /**
     * Convertit une entité Administrateur en AdminResponseDTO (données renvoyées au client).
     * Le mot de passe n'est jamais inclus dans la réponse.
     */
    private AdminResponseDTO toDTO(Administrateur admin) {
        return AdminResponseDTO.builder()
                .idAdmin(admin.getIdAdmin())
                .nom(admin.getNom())
                .email(admin.getEmail())
                .role(admin.getRole())
                .build();
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────
    @Override
    public AdminResponseDTO ajouterAdmin(AdminRequestDTO dto) {
        // Vérification : email déjà utilisé ?
        if (adminRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un administrateur avec cet email existe déjà : " + dto.getEmail());
        }

        Administrateur admin = toEntity(dto);
        Administrateur saved = adminRepository.save(admin);
        return toDTO(saved);
    }

    // ─── READ ALL ─────────────────────────────────────────────────────────────
    @Override
    public List<AdminResponseDTO> getAllAdmins() {
        // findAll() retourne la liste de toutes les entités
        // stream() + map() → convertit chaque entité en DTO
        return adminRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── READ BY ID ───────────────────────────────────────────────────────────
    @Override
    public AdminResponseDTO getAdminById(Long id) {
        // orElseThrow → lance une exception si l'admin n'est pas trouvé
        Administrateur admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrateur introuvable avec l'id : " + id));
        return toDTO(admin);
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────
    @Override
    public AdminResponseDTO updateAdmin(Long id, AdminRequestDTO dto) {
        // Vérifier que l'admin existe
        Administrateur admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrateur introuvable avec l'id : " + id));

        // Si l'email change, vérifier qu'il n'est pas déjà pris par un autre admin
        if (!admin.getEmail().equals(dto.getEmail())
                && adminRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé : " + dto.getEmail());
        }

        // Mise à jour des champs
        admin.setNom(dto.getNom());
        admin.setEmail(dto.getEmail());
        admin.setRole(dto.getRole());
        // Note : le mot de passe n'est pas mis à jour ici
        // → Prévoir un endpoint séparé /change-password pour plus de sécurité

        return toDTO(adminRepository.save(admin));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────
    @Override
    public void deleteAdmin(Long id) {
        // Vérifier que l'admin existe avant de supprimer
        if (!adminRepository.existsById(id)) {
            throw new RuntimeException("Administrateur introuvable avec l'id : " + id);
        }
        adminRepository.deleteById(id);
    }
}