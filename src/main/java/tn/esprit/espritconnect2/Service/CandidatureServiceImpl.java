package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.CandidatureApplyMeDTO;
import tn.esprit.espritconnect2.DTO.CandidatureRequestDTO;
import tn.esprit.espritconnect2.DTO.CandidatureResponseDTO;
import tn.esprit.espritconnect2.Entitie.Candidature;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Offre;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.exception.BusinessRuleException;
import tn.esprit.espritconnect2.exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.CandidatureRepository;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.OffreRepository;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidatureServiceImpl implements ICandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final EtudiantRepository etudiantRepository;
    private final OffreRepository offreRepository;

    @Override
    public CandidatureResponseDTO create(CandidatureRequestDTO dto) {
        Etudiant etudiant = etudiantRepository.findById(dto.getEtudiantId())
                .orElseThrow(() -> new NotFoundException("Etudiant introuvable avec id: " + dto.getEtudiantId()));
        Offre offre = offreRepository.findById(dto.getOffreId())
                .orElseThrow(() -> new NotFoundException("Offre introuvable avec id: " + dto.getOffreId()));

        boolean exists = candidatureRepository.existsByEtudiantIdEtudiantAndOffreIdOffre(
                dto.getEtudiantId(), dto.getOffreId()
        );
        if (exists) {
            throw new BusinessRuleException("Candidature deja existante pour cet etudiant et cette offre");
        }

        Candidature candidature = new Candidature();
        candidature.setDateCandidature(new Date());
        candidature.setStatutCandidature(Status.EN_ATTENTE);
        candidature.setLettreMotivation(dto.getLettreMotivation());
        candidature.setScoreMatch(0F);
        candidature.setEtudiant(etudiant);
        candidature.setOffre(offre);

        return toDTO(candidatureRepository.save(candidature));
    }

    @Override
    public CandidatureResponseDTO createForEmail(String email, CandidatureApplyMeDTO dto) {
        Etudiant etudiant = etudiantRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException(
                        "Student profile not found. Job applications are available for student accounts."));
        CandidatureRequestDTO request = new CandidatureRequestDTO();
        request.setEtudiantId(etudiant.getIdEtudiant());
        request.setOffreId(dto.getOffreId());
        request.setLettreMotivation(dto.getLettreMotivation());
        return create(request);
    }

    @Override
    public List<CandidatureResponseDTO> getByEtudiant(Long etudiantId) {
        if (!etudiantRepository.existsById(etudiantId)) {
            throw new NotFoundException("Etudiant introuvable avec id: " + etudiantId);
        }
        return candidatureRepository.findByEtudiantIdEtudiant(etudiantId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<CandidatureResponseDTO> getByOffre(Long offreId) {
        if (!offreRepository.existsById(offreId)) {
            throw new NotFoundException("Offre introuvable avec id: " + offreId);
        }
        return candidatureRepository.findByOffreIdOffre(offreId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public CandidatureResponseDTO updateStatus(Long candidatureId, Status status) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new NotFoundException("Candidature introuvable avec id: " + candidatureId));

        if (candidature.getStatutCandidature() != Status.EN_ATTENTE) {
            throw new BusinessRuleException("Seules les candidatures EN_ATTENTE peuvent changer de statut");
        }

        candidature.setStatutCandidature(status);
        return toDTO(candidatureRepository.save(candidature));
    }

    @Override
    public void cancel(Long candidatureId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new NotFoundException("Candidature introuvable avec id: " + candidatureId));

        if (candidature.getStatutCandidature() != Status.EN_ATTENTE) {
            throw new BusinessRuleException("Impossible de supprimer une candidature deja traitee");
        }

        candidatureRepository.delete(candidature);
    }

    private CandidatureResponseDTO toDTO(Candidature c) {
        return CandidatureResponseDTO.builder()
                .id(c.getId())
                .dateCandidature(c.getDateCandidature())
                .statutCandidature(c.getStatutCandidature())
                .lettreMotivation(c.getLettreMotivation())
                .scoreMatch(c.getScoreMatch())
                .etudiantId(c.getEtudiant() != null ? c.getEtudiant().getIdEtudiant() : null)
                .offreId(c.getOffre() != null ? c.getOffre().getIdOffre() : null)
                .jobTitle(c.getOffre() != null ? c.getOffre().getTitre() : null)
                .companyName(c.getOffre() != null && c.getOffre().getEntreprise() != null
                        ? c.getOffre().getEntreprise().getNom() : null)
                .fichierId(c.getFichier() != null ? c.getFichier().getIdFichier() : null)
                .build();
    }
}
