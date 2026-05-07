package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.MatchingResponseDTO;
import tn.esprit.espritconnect2.Entitie.Competence;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Matching;
import tn.esprit.espritconnect2.Entitie.Offre;
import tn.esprit.espritconnect2.Exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.MatchingRepository;
import tn.esprit.espritconnect2.Repository.OffreRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements IMatchingService {

    private final MatchingRepository matchingRepository;
    private final EtudiantRepository etudiantRepository;
    private final OffreRepository offreRepository;

    @Override
    public MatchingResponseDTO computeMatching(Long etudiantId, Long offreId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new NotFoundException("Etudiant introuvable avec id: " + etudiantId));
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new NotFoundException("Offre introuvable avec id: " + offreId));

        Set<String> studentSkills = etudiant.getCompetences() == null
                ? Set.of()
                : etudiant.getCompetences().stream()
                .map(Competence::getLibelle)
                .map(this::normalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        Set<String> offerKeywords = extractKeywords(offre.getTitre() + " " + offre.getDescription());

        List<String> missingSkills = offerKeywords.stream()
                .filter(k -> !studentSkills.contains(k))
                .sorted()
                .toList();

        float score = calculateScore(studentSkills, offerKeywords, etudiant, offre);
        List<String> recommendations = buildRecommendations(missingSkills);

        Matching matching = matchingRepository
                .findByEtudiantIdEtudiantAndOffreIdOffre(etudiantId, offreId)
                .orElse(new Matching());

        matching.setEtudiant(etudiant);
        matching.setOffre(offre);
        matching.setTypeMatching("ETUDIANT_OFFRE");
        matching.setDateCalcul(new Date());
        matching.setScoreCompatibilite(score);
        matching.setCompetencesRequises(missingSkills);
        matching.setRecommandations(recommendations);

        return toDTO(matchingRepository.save(matching));
    }

    @Override
    public List<MatchingResponseDTO> recomputeForEtudiant(Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new NotFoundException("Etudiant introuvable avec id: " + etudiantId));

        if (etudiant.getCandidatures() == null || etudiant.getCandidatures().isEmpty()) {
            return List.of();
        }

        Set<Long> offerIds = etudiant.getCandidatures().stream()
                .map(c -> c.getOffre().getIdOffre())
                .collect(Collectors.toSet());

        List<MatchingResponseDTO> results = new ArrayList<>();
        for (Long offerId : offerIds) {
            results.add(computeMatching(etudiantId, offerId));
        }
        return results;
    }

    @Override
    public List<MatchingResponseDTO> getByEtudiant(Long etudiantId) {
        if (!etudiantRepository.existsById(etudiantId)) {
            throw new NotFoundException("Etudiant introuvable avec id: " + etudiantId);
        }
        return matchingRepository.findByEtudiantIdEtudiantOrderByScoreCompatibiliteDesc(etudiantId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<MatchingResponseDTO> getTopByOffre(Long offreId, int limit) {
        if (!offreRepository.existsById(offreId)) {
            throw new NotFoundException("Offre introuvable avec id: " + offreId);
        }
        int safeLimit = Math.max(limit, 1);
        return matchingRepository.findByOffreIdOffreOrderByScoreCompatibiliteDesc(offreId).stream()
                .limit(safeLimit)
                .map(this::toDTO)
                .toList();
    }

    private float calculateScore(Set<String> studentSkills, Set<String> offerKeywords, Etudiant etudiant, Offre offre) {
        if (offerKeywords.isEmpty()) {
            return 50F;
        }

        long overlap = offerKeywords.stream().filter(studentSkills::contains).count();
        float skillsScore = (float) overlap / offerKeywords.size();

        float filiereBonus = 0F;
        if (etudiant.getFiliere() != null && offre.getDescription() != null &&
                offre.getDescription().toLowerCase(Locale.ROOT).contains(etudiant.getFiliere().toLowerCase(Locale.ROOT))) {
            filiereBonus = 0.1F;
        }

        float readiness = etudiant.getScoreReadiness() == null
                ? 0.5F
                : Math.min(etudiant.getScoreReadiness(), 100) / 100F;

        float total = (skillsScore * 0.6F) + (readiness * 0.3F) + (filiereBonus * 0.1F);
        return Math.min(100F, Math.max(0F, total * 100F));
    }

    private List<String> buildRecommendations(List<String> missingSkills) {
        if (missingSkills.isEmpty()) {
            return List.of("Votre profil correspond bien a cette offre.");
        }
        return missingSkills.stream()
                .limit(5)
                .map(skill -> "Ameliorer la competence: " + skill)
                .toList();
    }

    private Set<String> extractKeywords(String rawText) {
        if (rawText == null) {
            return Set.of();
        }
        String[] tokens = rawText.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .split("\\s+");

        Set<String> keywords = new HashSet<>();
        for (String token : tokens) {
            String cleaned = normalize(token);
            if (cleaned.length() >= 3) {
                keywords.add(cleaned);
            }
        }
        return keywords;
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private MatchingResponseDTO toDTO(Matching m) {
        return MatchingResponseDTO.builder()
                .idMatching(m.getIdMatching())
                .scoreCompatibilite(m.getScoreCompatibilite())
                .typeMatching(m.getTypeMatching())
                .dateCalcul(m.getDateCalcul())
                .competencesRequises(m.getCompetencesRequises())
                .recommandations(m.getRecommandations())
                .etudiantId(m.getEtudiant() != null ? m.getEtudiant().getIdEtudiant() : null)
                .offreId(m.getOffre() != null ? m.getOffre().getIdOffre() : null)
                .build();
    }
}
