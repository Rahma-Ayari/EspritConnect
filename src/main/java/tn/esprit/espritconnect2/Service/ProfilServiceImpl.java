package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.ProfilRequestDTO;
import tn.esprit.espritconnect2.DTO.ProfilResponseDTO;
import tn.esprit.espritconnect2.Entitie.Profil;
import tn.esprit.espritconnect2.Repository.ProfilRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service Profil.
 *
 * @Service   → Spring gère cette classe comme un bean de la couche service.
 * @RequiredArgsConstructor → injection du repository via constructeur (pas @Autowired).
 */
@Service
@RequiredArgsConstructor
public class ProfilServiceImpl implements IProfilService {

    private final ProfilRepository profilRepository;

    // ─── Mapper DTO → Entité ──────────────────────────────────────────────────
    /**
     * Convertit un ProfilRequestDTO en entité Profil.
     * On ne touche pas aux relations (etudiant, alumni...) ici :
     * elles sont gérées côté Etudiant/Alumni lors de leur propre création.
     */
    private Profil toEntity(ProfilRequestDTO dto) {
        Profil profil = new Profil();
        profil.setUserId(dto.getUserId());
        profil.setPhoto(dto.getPhoto());
        profil.setLienLinkedIn(dto.getLienLinkedIn());
        profil.setBio(dto.getBio());
        profil.setLienGitHub(dto.getLienGitHub());
        profil.setPrenom(dto.getPrenom());
        profil.setTelephone(dto.getTelephone());
        profil.setAdresse(dto.getAdresse());
        profil.setVille(dto.getVille());
        profil.setPays(dto.getPays());
        profil.setCodePostal(dto.getCodePostal());
        profil.setSiteWeb(dto.getSiteWeb());
        profil.setDateNaissance(dto.getDateNaissance());
        profil.setGenre(dto.getGenre());
        return profil;
    }

    // ─── Mapper Entité → ResponseDTO ─────────────────────────────────────────
    /**
     * Convertit une entité Profil en ProfilResponseDTO.
     * On détecte automatiquement le type et le nom du propriétaire
     * en vérifiant quelle relation est non-nulle.
     */
    private ProfilResponseDTO toDTO(Profil profil) {

        // Déterminer dynamiquement le propriétaire du profil
        String nomProprietaire = "Inconnu";
        String typeProprietaire = "INCONNU";

        if (profil.getEtudiant() != null) {
            nomProprietaire = profil.getEtudiant().getNom();
            typeProprietaire = "ETUDIANT";
        } else if (profil.getAlumni() != null) {
            // Adapter selon les champs de ton entité Alumni
            nomProprietaire = profil.getAlumni().getNom();
            typeProprietaire = "ALUMNI";
        } else if (profil.getEntreprise() != null) {
            // Adapter selon les champs de ton entité Entreprise
            nomProprietaire = profil.getEntreprise().getNom();
            typeProprietaire = "ENTREPRISE";
        } else if (profil.getAdministrateur() != null) {
            nomProprietaire = profil.getAdministrateur().getNom();
            typeProprietaire = "ADMINISTRATEUR";
        }

        return ProfilResponseDTO.builder()
                .idProfil(profil.getIdProfil())
                .userId(profil.getUserId())
                .photo(profil.getPhoto())
                .lienLinkedIn(profil.getLienLinkedIn())
                .bio(profil.getBio())
                .lienGitHub(profil.getLienGitHub())
                .prenom(profil.getPrenom())
                .telephone(profil.getTelephone())
                .adresse(profil.getAdresse())
                .ville(profil.getVille())
                .pays(profil.getPays())
                .codePostal(profil.getCodePostal())
                .siteWeb(profil.getSiteWeb())
                .dateNaissance(profil.getDateNaissance())
                .genre(profil.getGenre())
                .nomProprietaire(nomProprietaire)
                .typeProprietaire(typeProprietaire)
                .build();
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────
    @Override
    public ProfilResponseDTO creerProfil(ProfilRequestDTO dto) {
        // Vérifier que le userId n'est pas déjà utilisé
        if (dto.getUserId() != null && profilRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("Un profil avec ce userId existe déjà : " + dto.getUserId());
        }

        Profil profil = toEntity(dto);
        Profil saved = profilRepository.save(profil);
        return toDTO(saved);
    }

    // ─── READ ALL ─────────────────────────────────────────────────────────────
    @Override
    public List<ProfilResponseDTO> getAllProfils() {
        return profilRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── READ BY ID ───────────────────────────────────────────────────────────
    @Override
    public ProfilResponseDTO getProfilById(Long id) {
        Profil profil = profilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profil introuvable avec l'id : " + id));
        return toDTO(profil);
    }

    // ─── READ BY USER ID ──────────────────────────────────────────────────────
    @Override
    public ProfilResponseDTO getProfilByUserId(String userId) {
        Profil profil = profilRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable pour le userId : " + userId));
        return toDTO(profil);
    }

    // ─── READ CURRENT USER PROFILE ─────────────────────────────────────────────
    @Override
    public ProfilResponseDTO getCurrentUserProfile(String email) {
        Profil profil = profilRepository.findByUserId(email)
                .orElse(null);
        if (profil == null) {
            // Retourner un profil vide si l'utilisateur n'a pas encore de profil
            return ProfilResponseDTO.builder()
                    .userId(email)
                    .nomProprietaire("Utilisateur")
                    .typeProprietaire("INCONNU")
                    .build();
        }
        return toDTO(profil);
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────
    @Override
    public ProfilResponseDTO updateProfil(Long id, ProfilRequestDTO dto) {
        Profil profil = profilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profil introuvable avec l'id : " + id));

        // Vérifier unicité du userId seulement s'il a changé
        if (dto.getUserId() != null
                && !dto.getUserId().equals(profil.getUserId())
                && profilRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("Ce userId est déjà utilisé : " + dto.getUserId());
        }

        // Mise à jour uniquement des champs simples
        // Les relations (etudiant, alumni...) ne sont PAS modifiées ici
        profil.setUserId(dto.getUserId());
        profil.setPhoto(dto.getPhoto());
        profil.setLienLinkedIn(dto.getLienLinkedIn());
        profil.setBio(dto.getBio());
        profil.setLienGitHub(dto.getLienGitHub());
        profil.setPrenom(dto.getPrenom());
        profil.setTelephone(dto.getTelephone());
        profil.setAdresse(dto.getAdresse());
        profil.setVille(dto.getVille());
        profil.setPays(dto.getPays());
        profil.setCodePostal(dto.getCodePostal());
        profil.setSiteWeb(dto.getSiteWeb());
        profil.setDateNaissance(dto.getDateNaissance());
        profil.setGenre(dto.getGenre());

        return toDTO(profilRepository.save(profil));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────
    @Override
    public void deleteProfil(Long id) {
        if (!profilRepository.existsById(id)) {
            throw new RuntimeException("Profil introuvable avec l'id : " + id);
        }
        profilRepository.deleteById(id);
    }
}