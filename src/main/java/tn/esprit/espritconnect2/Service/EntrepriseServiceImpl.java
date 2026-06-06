package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.EntrepriseRequestDTO;
import tn.esprit.espritconnect2.DTO.EntrepriseResponseDTO;
import tn.esprit.espritconnect2.Entitie.Entreprise;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.EntrepriseRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntrepriseServiceImpl implements IEntrepriseService {

    private final EntrepriseRepository entrepriseRepository;
    private final UserRepository userRepository;

    private Entreprise toEntity(EntrepriseRequestDTO dto) {
        Entreprise e = new Entreprise();
        e.setNom(dto.getNom());
        e.setEmail(dto.getEmail());
        e.setPassword(dto.getPassword());
        e.setSecteur(dto.getSecteur());
        e.setSiteWeb(dto.getSiteWeb());
        e.setDescription(dto.getDescription());
        e.setValide(false); // Par défaut non valide
        e.setInscriptionRefusee(false);
        return e;
    }

    private EntrepriseResponseDTO toDTO(Entreprise e) {
        return EntrepriseResponseDTO.builder()
                .idEntreprise(e.getIdEntreprise())
                .nom(e.getNom())
                .email(e.getEmail())
                .secteur(e.getSecteur())
                .siteWeb(e.getSiteWeb())
                .valide(e.getValide())
                .description(e.getDescription())
                .build();
    }

    @Override
    public EntrepriseResponseDTO createEntreprise(EntrepriseRequestDTO dto) {
        if (entrepriseRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        Entreprise entreprise = toEntity(dto);
        return toDTO(entrepriseRepository.save(entreprise));
    }

    @Override
    public List<EntrepriseResponseDTO> getAllEntreprises() {
        return entrepriseRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EntrepriseResponseDTO getEntrepriseById(Long id) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));
        return toDTO(entreprise);
    }

    @Override
    public EntrepriseResponseDTO getEntrepriseByEmail(String email) {
        return toDTO(findOrCreateEntrepriseForUserEmail(email));
    }

    /**
     * Enterprise sign-up creates a {@link User} row only; job offers need an {@link Entreprise} row.
     * Sync from the authenticated user on first access.
     */
    private Entreprise findOrCreateEntrepriseForUserEmail(String email) {
        return entrepriseRepository.findByEmail(email)
                .orElseGet(() -> createEntrepriseFromUser(email));
    }

    private Entreprise createEntrepriseFromUser(String email) {
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getRole() == Role.ENTREPRISE)
                .orElseThrow(() -> new RuntimeException("Aucun compte entreprise associé à cet email"));

        Entreprise entreprise = new Entreprise();
        entreprise.setNom(user.getNom() != null && !user.getNom().isBlank() ? user.getNom() : "Entreprise");
        entreprise.setEmail(user.getEmail());
        entreprise.setPassword(user.getPassword());
        entreprise.setSecteur(user.getCompanySector());
        entreprise.setSiteWeb(user.getCompanyWebsite());
        entreprise.setDescription(user.getCompanyDescription());
        entreprise.setValide(user.isEnabled());
        entreprise.setInscriptionRefusee(user.isInscriptionRefusee());
        return entrepriseRepository.save(entreprise);
    }

    @Override
    public EntrepriseResponseDTO updateEntreprise(Long id, EntrepriseRequestDTO dto) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));
        
        if (!entreprise.getEmail().equals(dto.getEmail()) && entrepriseRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        entreprise.setNom(dto.getNom());
        entreprise.setEmail(dto.getEmail());
        entreprise.setSecteur(dto.getSecteur());
        entreprise.setSiteWeb(dto.getSiteWeb());
        entreprise.setDescription(dto.getDescription());
        // On ne change pas le mot de passe ici par sécurité

        return toDTO(entrepriseRepository.save(entreprise));
    }

    @Override
    public void deleteEntreprise(Long id) {
        if (!entrepriseRepository.existsById(id)) {
            throw new RuntimeException("Entreprise introuvable");
        }
        entrepriseRepository.deleteById(id);
    }
}
