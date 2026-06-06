package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.EntrepriseJobDashboardDTO;
import tn.esprit.espritconnect2.Entitie.Entreprise;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.Exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.CandidatureRepository;
import tn.esprit.espritconnect2.Repository.EntrepriseRepository;
import tn.esprit.espritconnect2.Repository.OffreRepository;

@Service
@RequiredArgsConstructor
public class EntrepriseJobDashboardService {

    private final EntrepriseRepository entrepriseRepository;
    private final OffreRepository offreRepository;
    private final CandidatureRepository candidatureRepository;
    private final EntrepriseVerificationService verificationService;

    public EntrepriseJobDashboardDTO getOverview(Long entrepriseId) {
        Entreprise e = entrepriseRepository.findById(entrepriseId)
                .orElseThrow(() -> new NotFoundException("Entreprise introuvable avec id: " + entrepriseId));

        long activeOffers = offreRepository.findByEntreprise_IdEntrepriseOrderByDatePublicationDesc(entrepriseId)
                .stream()
                .filter(o -> o.getStatutOfrre() != Status.REFUSEE)
                .count();

        long totalApplications = offreRepository.findByEntreprise_IdEntrepriseOrderByDatePublicationDesc(entrepriseId)
                .stream()
                .mapToLong(o -> candidatureRepository.findByOffreIdOffre(o.getIdOffre()).size())
                .sum();

        long pendingApplications = offreRepository.findByEntreprise_IdEntrepriseOrderByDatePublicationDesc(entrepriseId)
                .stream()
                .flatMap(o -> candidatureRepository.findByOffreIdOffre(o.getIdOffre()).stream())
                .filter(c -> c.getStatutCandidature() == Status.EN_ATTENTE)
                .count();

        return EntrepriseJobDashboardDTO.builder()
                .verification(verificationService.getStatus(entrepriseId))
                .activeOffers(activeOffers)
                .totalApplications(totalApplications)
                .pendingApplications(pendingApplications)
                .build();
    }
}
