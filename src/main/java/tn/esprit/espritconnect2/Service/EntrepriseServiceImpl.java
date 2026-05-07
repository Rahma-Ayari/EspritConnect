package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.EntrepriseRequestDTO;
import tn.esprit.espritconnect2.DTO.EntrepriseResponseDTO;
import tn.esprit.espritconnect2.Entitie.Entreprise;
import tn.esprit.espritconnect2.Repository.EntrepriseRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntrepriseServiceImpl implements IEntrepriseService {

    private final EntrepriseRepository entrepriseRepository;

    private Entreprise toEntity(EntrepriseRequestDTO dto) {
        Entreprise e = new Entreprise();
        e.setNom(dto.getNom());
        e.setEmail(dto.getEmail());
        e.setPassword(dto.getPassword());
        e.setSecteur(dto.getSecteur());
        e.setSiteWeb(dto.getSiteWeb());
        e.setDescription(dto.getDescription());
        e.setValide(false); // Par défaut non valide
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
