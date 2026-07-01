package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tn.esprit.espritconnect2.DTO.CandidatureApplyMeDTO;
import tn.esprit.espritconnect2.DTO.CandidatureRequestDTO;
import tn.esprit.espritconnect2.DTO.CandidatureResponseDTO;
import tn.esprit.espritconnect2.Entitie.Candidature;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Offre;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.exception.BusinessRuleException;
import tn.esprit.espritconnect2.exception.NotFoundException;
import tn.esprit.espritconnect2.Entitie.Fichier;
import tn.esprit.espritconnect2.Repository.CandidatureRepository;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.FichierRepository;
import tn.esprit.espritconnect2.Repository.OffreRepository;
import tn.esprit.espritconnect2.Service.StudentAccountResolver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidatureServiceImpl implements ICandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final EtudiantRepository etudiantRepository;
    private final OffreRepository offreRepository;
    private final FichierRepository fichierRepository;
    private final IEmailService emailService;
    private final StudentAccountResolver studentAccountResolver;

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
        candidature.setYearsExperience(dto.getYearsExperience());
        candidature.setWillingToRelocate(dto.getWillingToRelocate());
        candidature.setAvailabilityDate(parseDate(dto.getAvailabilityDate()));

        Candidature saved = candidatureRepository.save(candidature);
        attachFichier(saved, dto.getFichierId(), etudiant.getIdEtudiant());
        String companyName = offre.getEntreprise() != null ? offre.getEntreprise().getNom() : null;
        String recipient = resolveConfirmationEmail(etudiant, dto.getNotifyEmail());
        if (recipient != null) {
            emailService.sendApplicationConfirmation(recipient, etudiant.getNom(), offre.getTitre(), companyName);
        }
        return toDTO(saved);
    }

    private String resolveConfirmationEmail(Etudiant etudiant, String preferredEmail) {
        if (StringUtils.hasText(preferredEmail)) {
            return preferredEmail.trim();
        }
        if (StringUtils.hasText(etudiant.getEmail())) {
            return etudiant.getEmail().trim();
        }
        log.error("No email available to send application confirmation for etudiant id={}", etudiant.getIdEtudiant());
        return null;
    }

    @Override
    public CandidatureResponseDTO createForEmail(String email, CandidatureApplyMeDTO dto) {
        Etudiant etudiant = studentAccountResolver.resolveOrProvision(email);
        if (!StringUtils.hasText(etudiant.getEmail()) || !email.equalsIgnoreCase(etudiant.getEmail().trim())) {
            etudiant.setEmail(email.trim());
            etudiantRepository.save(etudiant);
        }
        CandidatureRequestDTO request = new CandidatureRequestDTO();
        request.setEtudiantId(etudiant.getIdEtudiant());
        request.setOffreId(dto.getOffreId());
        request.setLettreMotivation(dto.getLettreMotivation());
        request.setFichierId(dto.getFichierId());
        request.setYearsExperience(dto.getYearsExperience());
        request.setWillingToRelocate(dto.getWillingToRelocate());
        request.setAvailabilityDate(dto.getAvailabilityDate());
        request.setNotifyEmail(email.trim());
        return create(request);
    }

    private void attachFichier(Candidature candidature, Long fichierId, Long etudiantId) {
        if (fichierId == null) {
            return;
        }
        Fichier fichier = fichierRepository.findById(fichierId)
                .orElseThrow(() -> new NotFoundException("Fichier introuvable avec id: " + fichierId));
        if (fichier.getEtudiant() != null && etudiantId != null
                && !etudiantId.equals(fichier.getEtudiant().getIdEtudiant())) {
            throw new BusinessRuleException("Ce fichier n'appartient pas a cet etudiant");
        }
        fichier.setCandidature(candidature);
        fichierRepository.save(fichier);
    }

    private Date parseDate(String iso) {
        if (iso == null || iso.isBlank()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(iso.trim());
        } catch (Exception e) {
            return null;
        }
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
        return candidatureRepository.findByOffreIdWithEtudiant(offreId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public CandidatureResponseDTO updateStatus(Long candidatureId, Status status) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new NotFoundException("Candidature introuvable avec id: " + candidatureId));

        validateStatusTransition(candidature.getStatutCandidature(), status);

        candidature.setStatutCandidature(status);
        Candidature saved = candidatureRepository.save(candidature);
        notifyStudentOfStatusChange(saved, status);
        return toDTO(saved);
    }

    private void validateStatusTransition(Status current, Status target) {
        if (current == Status.ACCEPTEE || current == Status.REFUSEE) {
            throw new BusinessRuleException("This application has already been finalized");
        }
        if (current == Status.EN_ATTENTE
                && (target == Status.EN_ENTRETIEN || target == Status.ACCEPTEE || target == Status.REFUSEE)) {
            return;
        }
        if (current == Status.EN_ENTRETIEN && (target == Status.ACCEPTEE || target == Status.REFUSEE)) {
            return;
        }
        throw new BusinessRuleException("Status transition not allowed: " + current + " -> " + target);
    }

    private void notifyStudentOfStatusChange(Candidature candidature, Status status) {
        Etudiant etudiant = candidature.getEtudiant();
        Offre offre = candidature.getOffre();
        if (etudiant == null || offre == null) {
            log.warn("Cannot notify student for candidature {}: missing etudiant or offre", candidature.getId());
            return;
        }

        String email = resolveConfirmationEmail(etudiant, etudiant.getEmail());
        if (email == null) {
            return;
        }

        String studentName = etudiant.getNom();
        String jobTitle = offre.getTitre();
        String companyName = offre.getEntreprise() != null ? offre.getEntreprise().getNom() : "the company";

        switch (status) {
            case EN_ENTRETIEN -> emailService.sendApplicationProceeding(email, studentName, jobTitle, companyName);
            case ACCEPTEE -> emailService.sendApplicationAccepted(email, studentName, jobTitle, companyName);
            case REFUSEE -> emailService.sendApplicationRejected(email, studentName, jobTitle, companyName);
            default -> log.debug("No notification email for candidature status {}", status);
        }
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
                .etudiantNom(c.getEtudiant() != null ? c.getEtudiant().getNom() : null)
                .etudiantEmail(c.getEtudiant() != null ? c.getEtudiant().getEmail() : null)
                .filiere(c.getEtudiant() != null ? c.getEtudiant().getFiliere() : null)
                .offreId(c.getOffre() != null ? c.getOffre().getIdOffre() : null)
                .jobTitle(c.getOffre() != null ? c.getOffre().getTitre() : null)
                .companyName(c.getOffre() != null && c.getOffre().getEntreprise() != null
                        ? c.getOffre().getEntreprise().getNom() : null)
                .fichierId(c.getFichier() != null ? c.getFichier().getIdFichier() : null)
                .build();
    }
}
