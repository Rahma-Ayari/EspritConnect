package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.OffreRequestDTO;
import tn.esprit.espritconnect2.DTO.OffreResponseDTO;
import tn.esprit.espritconnect2.Entitie.Entreprise;
import tn.esprit.espritconnect2.Entitie.Offre;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.Repository.EntrepriseRepository;
import tn.esprit.espritconnect2.Repository.OffreRepository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OffreServiceImpl implements IOffreService {

    private final OffreRepository offreRepository;
    private final EntrepriseRepository entrepriseRepository;

    private Offre toEntity(OffreRequestDTO dto) {
        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));
        
        Offre o = new Offre();
        o.setTitre(dto.getTitre());
        o.setDescription(dto.getDescription());
        o.setTypeOffre(dto.getTypeOffre());
        o.setLocalisation(dto.getLocalisation());
        o.setEntreprise(entreprise);
        o.setStatutOfrre(Status.EN_ATTENTE);
        o.setDatePublication(new Date());
        return o;
    }

    private OffreResponseDTO toDTO(Offre o) {
        return OffreResponseDTO.builder()
                .idOffre(o.getIdOffre())
                .titre(o.getTitre())
                .description(o.getDescription())
                .typeOffre(o.getTypeOffre())
                .localisation(o.getLocalisation())
                .statutOfrre(o.getStatutOfrre())
                .datePublication(o.getDatePublication())
                .entrepriseId(o.getEntreprise().getIdEntreprise())
                .entrepriseNom(o.getEntreprise().getNom())
                .build();
    }

    @Override
    public OffreResponseDTO createOffre(OffreRequestDTO dto) {
        Offre offre = toEntity(dto);
        return toDTO(offreRepository.save(offre));
    }

    @Override
    public List<OffreResponseDTO> getAllOffres() {
        return offreRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OffreResponseDTO getOffreById(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre introuvable"));
        return toDTO(offre);
    }

    @Override
    public OffreResponseDTO updateOffre(Long id, OffreRequestDTO dto) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre introuvable"));
        
        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));

        offre.setTitre(dto.getTitre());
        offre.setDescription(dto.getDescription());
        offre.setTypeOffre(dto.getTypeOffre());
        offre.setLocalisation(dto.getLocalisation());
        offre.setEntreprise(entreprise);

        return toDTO(offreRepository.save(offre));
    }

    @Override
    public void deleteOffre(Long id) {
        if (!offreRepository.existsById(id)) {
            throw new RuntimeException("Offre introuvable");
        }
        offreRepository.deleteById(id);
    }
}
