package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tn.esprit.espritconnect2.DTO.PostMatchResponse;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.Repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostMatchingService {

    private static final int MIN_SCORE = 50;

    private final PostMatchRepository postMatchRepository;
    private final EtudiantRepository etudiantRepository;
    private final AlumniRepository alumniRepository;

    @Transactional
    public void computeMatches(ForumPost post) {
        if (post == null || post.getId() == null || post.getTags() == null || post.getTags().isEmpty()) {
            return;
        }

        postMatchRepository.deleteByPostId(post.getId());
        Set<String> postTags = normalizeTags(post.getTags());
        if (postTags.isEmpty()) return;

        Map<String, MatchCandidate> candidates = new HashMap<>();

        etudiantRepository.findAll().forEach(etudiant -> {
            if (etudiant.getEmail() != null && !etudiant.getEmail().equalsIgnoreCase(post.getAuthorEmail())) {
                Set<String> skills = extractSkills(etudiant.getCompetences());
                int score = computeScore(postTags, skills);
                if (score >= MIN_SCORE) {
                    addCandidate(candidates, etudiant.getEmail(), etudiant.getNom(), Role.ETUDIANT, score);
                }
            }
        });

        alumniRepository.findAll().forEach(alumni -> {
            if (alumni.getEmail() != null && !alumni.getEmail().equalsIgnoreCase(post.getAuthorEmail())) {
                Set<String> skills = extractSkills(alumni.getCompetences());
                int score = computeScore(postTags, skills);
                if (score >= MIN_SCORE) {
                    addCandidate(candidates, alumni.getEmail(), alumni.getNom(), Role.ALUMNI, score);
                }
            }
        });

        candidates.values().stream()
                .sorted(Comparator.comparingInt(MatchCandidate::score).reversed())
                .limit(25)
                .forEach(candidate -> postMatchRepository.save(PostMatch.builder()
                        .post(post)
                        .matchedUserEmail(candidate.email())
                        .matchedUserName(candidate.name())
                        .matchedUserRole(candidate.role())
                        .score(candidate.score())
                        .notified(true)
                        .build()));

        log.info("Computed {} matches for post #{}", candidates.size(), post.getId());
    }

    @Transactional(readOnly = true)
    public List<PostMatchResponse> getMatchesForUser(String email) {
        if (!StringUtils.hasText(email)) return List.of();
        return postMatchRepository.findByMatchedUserEmailIgnoreCaseOrderByScoreDescCreatedAtDesc(email.trim())
                .stream()
                .map(m -> PostMatchResponse.builder()
                        .matchId(m.getId())
                        .post(m.getPost())
                        .score(m.getScore())
                        .notified(m.isNotified())
                        .build())
                .collect(Collectors.toList());
    }

    private void addCandidate(Map<String, MatchCandidate> map, String email, String name, Role role, int score) {
        String key = email.toLowerCase(Locale.ROOT);
        MatchCandidate existing = map.get(key);
        if (existing == null || score > existing.score()) {
            map.put(key, new MatchCandidate(email, name != null ? name : email, role, score));
        }
    }

    private int computeScore(Set<String> postTags, Set<String> userSkills) {
        if (userSkills.isEmpty()) return 0;
        long intersection = postTags.stream().filter(userSkills::contains).count();
        if (intersection == 0) return 0;
        return (int) Math.round((intersection * 100.0) / postTags.size());
    }

    private Set<String> normalizeTags(List<String> tags) {
        return tags.stream()
                .filter(StringUtils::hasText)
                .map(t -> t.toLowerCase(Locale.ROOT).replace("#", "").trim())
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toSet());
    }

    private Set<String> extractSkills(List<Competence> competences) {
        if (competences == null) return Set.of();
        return competences.stream()
                .filter(Objects::nonNull)
                .map(Competence::getLibelle)
                .filter(StringUtils::hasText)
                .map(s -> s.toLowerCase(Locale.ROOT).trim())
                .collect(Collectors.toSet());
    }

    private record MatchCandidate(String email, String name, Role role, int score) {}
}
