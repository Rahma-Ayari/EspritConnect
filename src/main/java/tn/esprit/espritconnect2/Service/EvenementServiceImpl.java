package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.EvenementRequestDTO;
import tn.esprit.espritconnect2.DTO.EvenementResponseDTO;
import tn.esprit.espritconnect2.Entitie.Entreprise;
import tn.esprit.espritconnect2.Entitie.Evenement;
import tn.esprit.espritconnect2.Repository.EntrepriseRepository;
import tn.esprit.espritconnect2.Repository.EvenementRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvenementServiceImpl implements IEvenementService {

    private final EvenementRepository evenementRepository;
    private final EntrepriseRepository entrepriseRepository;

    private Evenement toEntity(EvenementRequestDTO dto) {
        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));

        Evenement ev = new Evenement();
        ev.setTitre(dto.getTitre());
        ev.setLieu(dto.getLieu());
        ev.setDateEvenement(dto.getDateEvenement());
        ev.setCapacite(dto.getCapacite());
        ev.setType(dto.getType());
        ev.setEntreprise(entreprise);
        return ev;
    }

    private EvenementResponseDTO toDTO(Evenement ev) {
        return EvenementResponseDTO.builder()
                .idEvenement(ev.getIdEvenement())
                .titre(ev.getTitre())
                .lieu(ev.getLieu())
                .dateEvenement(ev.getDateEvenement())
                .capacite(ev.getCapacite())
                .type(ev.getType())
                .entrepriseId(ev.getEntreprise().getIdEntreprise())
                .entrepriseNom(ev.getEntreprise().getNom())
                .build();
    }

    @Override
    public EvenementResponseDTO createEvenement(EvenementRequestDTO dto) {
        Evenement evenement = toEntity(dto);
        return toDTO(evenementRepository.save(evenement));
    }

    @Override
    public List<EvenementResponseDTO> getAllEvenements() {
        return evenementRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EvenementResponseDTO getEvenementById(Long id) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));
        return toDTO(evenement);
    }

    @Override
    public EvenementResponseDTO updateEvenement(Long id, EvenementRequestDTO dto) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));

        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));

        evenement.setTitre(dto.getTitre());
        evenement.setLieu(dto.getLieu());
        evenement.setDateEvenement(dto.getDateEvenement());
        evenement.setCapacite(dto.getCapacite());
        evenement.setType(dto.getType());
        evenement.setEntreprise(entreprise);

        return toDTO(evenementRepository.save(evenement));
    }

    @Override
    public void deleteEvenement(Long id) {
        if (!evenementRepository.existsById(id)) {
            throw new RuntimeException("Événement introuvable");
        }
        evenementRepository.deleteById(id);
    }
}
