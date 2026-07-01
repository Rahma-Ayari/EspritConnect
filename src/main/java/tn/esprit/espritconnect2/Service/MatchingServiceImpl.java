package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.CandidateMatchDTO;
import tn.esprit.espritconnect2.DTO.MatchingResponseDTO;
import tn.esprit.espritconnect2.Entitie.Candidature;
import tn.esprit.espritconnect2.Entitie.Competence;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.ExperienceLevel;
import tn.esprit.espritconnect2.Entitie.Matching;
import tn.esprit.espritconnect2.Entitie.Niveau;
import tn.esprit.espritconnect2.Entitie.Offre;
import tn.esprit.espritconnect2.exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.CandidatureRepository;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.MatchingRepository;
import tn.esprit.espritconnect2.Repository.OffreRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingServiceImpl implements IMatchingService {

    private final MatchingRepository matchingRepository;
    private final EtudiantRepository etudiantRepository;
    private final OffreRepository offreRepository;
    private final CandidatureRepository candidatureRepository;

    @Override
    @Transactional
    public MatchingResponseDTO computeMatching(Long etudiantId, Long offreId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new NotFoundException("Etudiant introuvable avec id: " + etudiantId));
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new NotFoundException("Offre introuvable avec id: " + offreId));

        ProfileMatchScore profileScore = computeProfileMatch(etudiant, offre);
        List<String> missingSkills = profileScore.missingSkills();
        float score = profileScore.overall();
        List<String> recommendations = buildRecommendations(missingSkills);

        Matching matching = matchingRepository
                .findByEtudiantIdEtudiantAndOffreIdOffre(etudiantId, offreId)
                .orElse(new Matching());

        matching.setEtudiant(etudiant);
        matching.setOffre(offre);
        matching.setTypeMatching("ETUDIANT_OFFRE");
        matching.setDateCalcul(new Date());
        matching.setScoreCompatibilite(score);
        matching.setCompetencesRequises(new ArrayList<>(missingSkills));
        matching.setRecommandations(new ArrayList<>(recommendations));

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

    @Override
    @Transactional
    public List<CandidateMatchDTO> getRankedCandidatesForOffre(Long offreId, int limit) {
        if (!offreRepository.existsById(offreId)) {
            throw new NotFoundException("Offre introuvable avec id: " + offreId);
        }
        int safeLimit = Math.max(limit, 1);
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new NotFoundException("Offre introuvable avec id: " + offreId));
        List<Candidature> candidatures = candidatureRepository.findByOffreIdWithEtudiant(offreId);
        if (candidatures.isEmpty()) {
            return List.of();
        }

        List<CandidateMatchDTO> ranked = new ArrayList<>();

        for (Candidature candidature : candidatures) {
            try {
                CandidateMatchDTO dto = buildCandidateMatch(candidature, offre);
                if (dto != null) {
                    ranked.add(dto);
                }
            } catch (Exception ex) {
                log.warn("Failed to rank candidature {} for offre {}: {}",
                        candidature.getId(), offreId, ex.getMessage());
            }
        }

        candidatureRepository.saveAll(candidatures);

        ranked.sort((a, b) -> Float.compare(
                b.getScoreCompatibilite() != null ? b.getScoreCompatibilite() : 0F,
                a.getScoreCompatibilite() != null ? a.getScoreCompatibilite() : 0F
        ));
        return ranked.stream().limit(safeLimit).toList();
    }

    private CandidateMatchDTO buildCandidateMatch(Candidature candidature, Offre offre) {
        Etudiant etudiant = candidature.getEtudiant();
        if (etudiant == null) {
            log.warn("Candidature {} has no linked etudiant, skipping", candidature.getId());
            return null;
        }

        ProfileMatchScore profileScore = computeProfileMatch(etudiant, offre);
        List<String> missingSkills = profileScore.missingSkills();
        float score = profileScore.overall();
        List<String> recommendations = buildRecommendations(missingSkills);

        candidature.setScoreMatch(score);
        persistMatchingRecord(etudiant, offre, score, missingSkills, recommendations);

        return CandidateMatchDTO.builder()
                .candidatureId(candidature.getId())
                .etudiantId(etudiant.getIdEtudiant())
                .etudiantNom(etudiant.getNom())
                .etudiantEmail(etudiant.getEmail())
                .filiere(etudiant.getFiliere())
                .niveau(etudiant.getNiveau() != null ? etudiant.getNiveau().name() : null)
                .scoreCompatibilite(score)
                .skillsScore(profileScore.skills())
                .experienceScore(profileScore.experience())
                .educationScore(profileScore.education())
                .skillsMatched(profileScore.matchedSkills())
                .recommandations(recommendations)
                .lettreMotivationExcerpt(excerpt(candidature.getLettreMotivation()))
                .hasResume(candidature.getFichier() != null)
                .candidatureStatus(candidature.getStatutCandidature())
                .build();
    }

    /**
     * Same weighting as the student-side MatchScoreService (skills 50%, experience 30%, education 20%).
     */
    private ProfileMatchScore computeProfileMatch(Etudiant etudiant, Offre offre) {
        List<String> jobSkills = collectJobSkills(offre);
        Set<String> profileSkills = loadStudentSkills(etudiant);

        List<String> matchedSkills = new ArrayList<>();
        for (String jobSkill : jobSkills) {
            if (skillMatchesProfile(jobSkill, profileSkills)) {
                matchedSkills.add(jobSkill);
            }
        }

        int skillsPercent = jobSkills.isEmpty()
                ? 75
                : Math.round((matchedSkills.size() * 100F) / jobSkills.size());

        int experiencePercent = experienceScore(offre.getExperienceLevel(), etudiant.getNiveau());
        int educationPercent = educationScore(offre.getDepartment(), etudiant.getFiliere());

        int overall = Math.round(skillsPercent * 0.5F + experiencePercent * 0.3F + educationPercent * 0.2F);

        List<String> missingSkills = jobSkills.stream()
                .filter(js -> !skillMatchesProfile(js, profileSkills))
                .distinct()
                .sorted()
                .toList();

        return new ProfileMatchScore(overall, skillsPercent, experiencePercent, educationPercent, matchedSkills, missingSkills);
    }

    private List<String> collectJobSkills(Offre offre) {
        LinkedHashSet<String> skills = new LinkedHashSet<>();
        if (offre.getCompetencesRequises() != null) {
            offre.getCompetencesRequises().stream()
                    .map(this::normalize)
                    .filter(s -> !s.isBlank())
                    .forEach(skills::add);
        }
        if (offre.getTechnologies() != null) {
            offre.getTechnologies().stream()
                    .map(this::normalize)
                    .filter(s -> !s.isBlank())
                    .forEach(skills::add);
        }
        return new ArrayList<>(skills);
    }

    private boolean skillMatchesProfile(String jobSkill, Set<String> profileSkills) {
        for (String profileSkill : profileSkills) {
            if (jobSkill.contains(profileSkill) || profileSkill.contains(jobSkill)) {
                return true;
            }
        }
        return false;
    }

    private int experienceScore(ExperienceLevel jobLevel, Niveau profileNiveau) {
        int jobIdx = experienceLevelIndex(jobLevel);
        int profileIdx = niveauIndex(profileNiveau);
        int diff = Math.abs(jobIdx - profileIdx);
        return Math.max(40, 100 - diff * 25);
    }

    private int experienceLevelIndex(ExperienceLevel level) {
        if (level == null) {
            return 0;
        }
        return switch (level) {
            case JUNIOR -> 0;
            case INTERMEDIATE -> 1;
            case SENIOR -> 2;
            case EXPERT -> 3;
        };
    }

    private int niveauIndex(Niveau niveau) {
        if (niveau == null) {
            return 0;
        }
        return switch (niveau) {
            case DEBUTANT -> 0;
            case INTERMEDIAIRE -> 1;
            case EXPERT -> 3;
        };
    }

    private int educationScore(String department, String filiere) {
        if (department == null || department.isBlank() || filiere == null || filiere.isBlank()) {
            return 70;
        }
        String d = department.toLowerCase(Locale.ROOT);
        String f = filiere.toLowerCase(Locale.ROOT);
        if (d.contains("engineer") && (f.contains("info") || f.contains("gl"))) {
            return 92;
        }
        if (d.contains(f) || f.contains(d)) {
            return 88;
        }
        return 72;
    }

    private record ProfileMatchScore(
            float overall,
            int skills,
            int experience,
            int education,
            List<String> matchedSkills,
            List<String> missingSkills
    ) {}

    private void persistMatchingRecord(Etudiant etudiant, Offre offre, float score,
                                       List<String> missingSkills, List<String> recommendations) {
        try {
            Matching matching = matchingRepository
                    .findByEtudiantIdEtudiantAndOffreIdOffre(etudiant.getIdEtudiant(), offre.getIdOffre())
                    .orElse(new Matching());
            matching.setEtudiant(etudiant);
            matching.setOffre(offre);
            matching.setTypeMatching("ETUDIANT_OFFRE");
            matching.setDateCalcul(new Date());
            matching.setScoreCompatibilite(score);
            matching.setCompetencesRequises(new ArrayList<>(missingSkills));
            matching.setRecommandations(new ArrayList<>(recommendations));
            matchingRepository.save(matching);
        } catch (Exception ex) {
            log.debug("Could not persist matching for etudiant {} offre {}: {}",
                    etudiant.getIdEtudiant(), offre.getIdOffre(), ex.getMessage());
        }
    }

    private Set<String> loadStudentSkills(Etudiant etudiant) {
        try {
            if (etudiant.getCompetences() == null) {
                return Set.of();
            }
            return etudiant.getCompetences().stream()
                    .map(Competence::getLibelle)
                    .map(this::normalize)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());
        } catch (Exception ex) {
            log.debug("Could not load competences for etudiant {}: {}", etudiant.getIdEtudiant(), ex.getMessage());
            return Set.of();
        }
    }

    private String excerpt(String text) {
        if (text == null || text.isBlank()) {
            return "No cover letter provided.";
        }
        String trimmed = text.trim();
        return trimmed.length() <= 160 ? trimmed : trimmed.substring(0, 157) + "...";
    }

    private List<String> buildRecommendations(List<String> missingSkills) {
        if (missingSkills.isEmpty()) {
            return List.of("Your profile is a strong match for this offer.");
        }
        return missingSkills.stream()
                .limit(5)
                .map(skill -> "Improve skill: " + skill)
                .toList();
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
