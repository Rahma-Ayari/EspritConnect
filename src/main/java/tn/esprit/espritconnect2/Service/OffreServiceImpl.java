package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.OffreRequestDTO;
import tn.esprit.espritconnect2.DTO.OffreResponseDTO;
import tn.esprit.espritconnect2.Entitie.Entreprise;
import tn.esprit.espritconnect2.Entitie.Offre;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.Entitie.Type;
import tn.esprit.espritconnect2.exception.BusinessRuleException;
import tn.esprit.espritconnect2.exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.CandidatureRepository;
import tn.esprit.espritconnect2.Repository.EntrepriseRepository;
import tn.esprit.espritconnect2.Repository.OffreRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OffreServiceImpl implements IOffreService {

    private final OffreRepository offreRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final CandidatureRepository candidatureRepository;
    private final EntrepriseVerificationService verificationService;

    private Offre toEntity(OffreRequestDTO dto) {
        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new NotFoundException("Entreprise introuvable"));

        if (!verificationService.canPostOffers(entreprise)) {
            throw new BusinessRuleException(
                    "Entreprise non verifiee. Uploadez vos documents legaux et attendez la validation admin.");
        }

        Offre o = new Offre();
        applyFields(o, dto, entreprise);
        o.setStatutOfrre(Status.ACCEPTEE);
        o.setDatePublication(new Date());
        return o;
    }

    private void applyFields(Offre o, OffreRequestDTO dto, Entreprise entreprise) {
        o.setTitre(dto.getTitre());
        o.setDescription(dto.getDescription());
        o.setTypeOffre(dto.getTypeOffre());
        o.setLocalisation(dto.getLocalisation());
        o.setDomaine(dto.getDomaine());
        o.setCompetencesRequises(dto.getCompetencesRequises() != null
                ? new ArrayList<>(dto.getCompetencesRequises())
                : new ArrayList<>());
        o.setEntreprise(entreprise);
    }

    private OffreResponseDTO toDTO(Offre o) {
        long apps = candidatureRepository.findByOffreIdOffre(o.getIdOffre()).size();
        return OffreResponseDTO.builder()
                .idOffre(o.getIdOffre())
                .titre(o.getTitre())
                .description(o.getDescription())
                .typeOffre(o.getTypeOffre())
                .localisation(o.getLocalisation())
                .domaine(o.getDomaine())
                .competencesRequises(o.getCompetencesRequises())
                .applicationsCount(apps)
                .statutOfrre(o.getStatutOfrre())
                .datePublication(o.getDatePublication())
                .entrepriseId(o.getEntreprise().getIdEntreprise())
                .entrepriseNom(o.getEntreprise().getNom())
                .build();
    }

    @Override
    public OffreResponseDTO createOffre(OffreRequestDTO dto) {
        return toDTO(offreRepository.save(toEntity(dto)));
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
                .orElseThrow(() -> new NotFoundException("Offre introuvable"));
        return toDTO(offre);
    }

    @Override
    public OffreResponseDTO updateOffre(Long id, OffreRequestDTO dto) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable"));

        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new NotFoundException("Entreprise introuvable"));

        applyFields(offre, dto, entreprise);
        return toDTO(offreRepository.save(offre));
    }

    @Override
    public void deleteOffre(Long id) {
        if (!offreRepository.existsById(id)) {
            throw new NotFoundException("Offre introuvable");
        }
        offreRepository.deleteById(id);
    }

    @Override
    public List<OffreResponseDTO> getOffresByEntreprise(Long entrepriseId) {
        return offreRepository.findByEntreprise_IdEntrepriseOrderByDatePublicationDesc(entrepriseId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OffreResponseDTO> searchPublic(String domaine, String localisation, Type typeOffre) {
        String domaineNorm = domaine == null ? "" : domaine.trim().toLowerCase(Locale.ROOT);
        String locNorm = localisation == null ? "" : localisation.trim().toLowerCase(Locale.ROOT);

        return offreRepository.findAll().stream()
                .filter(o -> o.getStatutOfrre() == Status.ACCEPTEE)
                .filter(o -> typeOffre == null || o.getTypeOffre() == typeOffre)
                .filter(o -> domaineNorm.isEmpty()
                        || (o.getDomaine() != null && o.getDomaine().toLowerCase(Locale.ROOT).contains(domaineNorm))
                        || (o.getTitre() != null && o.getTitre().toLowerCase(Locale.ROOT).contains(domaineNorm)))
                .filter(o -> locNorm.isEmpty()
                        || (o.getLocalisation() != null && o.getLocalisation().toLowerCase(Locale.ROOT).contains(locNorm)))
                .map(this::toDTO)
                .toList();
    }
}
