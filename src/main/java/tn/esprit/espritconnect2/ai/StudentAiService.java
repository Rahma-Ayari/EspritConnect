package tn.esprit.espritconnect2.ai;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tn.esprit.espritconnect2.DTO.studentai.*;
import tn.esprit.espritconnect2.Entitie.Competence;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Offre;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.OffreRepository;
import tn.esprit.espritconnect2.Service.StudentAccountResolver;
import tn.esprit.espritconnect2.ai.prompts.*;
import tn.esprit.espritconnect2.exception.NotFoundException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentAiService {

    private static final int MAX_RESUME_CHARS = 12_000;
    private static final int JSON_MAX_TOKENS = 8192;

    private final JobsAiProperties properties;
    private final JobsLlmClient llmClient;
    private final JobsAiCache cache;
    private final OffreRepository offreRepository;
    private final EtudiantRepository etudiantRepository;
    private final StudentAccountResolver studentAccountResolver;

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public StudentJobMatchResponseDTO jobMatch(String email, StudentJobMatchRequestDTO request) {
        requireConfigured();
        validateOffreId(request.getOffreId());
        Etudiant etudiant = resolveStudent(email);
        Offre offre = loadOffre(request.getOffreId());

        String cacheKey = "student-match:" + offre.getIdOffre() + ":" + etudiant.getIdEtudiant()
                + ":" + hash(request.getResumeText());
        if (!request.isForceRefresh()) {
            Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                return wrapMatch(parseMatchJson(cached.get().payload()), cached.get().provider(), true, offre);
            }
        }

        String userPrompt = JobMatchingPrompt.user(
                buildStudentProfile(etudiant),
                buildJobDescription(offre),
                truncateForAi(request.getResumeText())
        );
        JobsLlmClient.LlmResult result = completeForJson(JobMatchingPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapMatch(parseMatchJson(result.text()), result.providerLabel(), false, offre);
    }

    public StudentResumeReviewResponseDTO reviewResume(String email, StudentResumeReviewRequestDTO request) {
        requireConfigured();
        requireResumeText(request.getResumeText());
        Etudiant etudiant = resolveStudent(email);

        String cacheKey = "student-review:" + etudiant.getIdEtudiant() + ":" + hash(request.getResumeText(), request.getTargetRole());
        if (!request.isForceRefresh()) {
            Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                return wrapReview(parseReviewJson(cached.get().payload()), cached.get().provider(), true);
            }
        }

        String userPrompt = ResumeReviewPrompt.user(truncateForAi(request.getResumeText()), request.getTargetRole());
        JobsLlmClient.LlmResult result = completeForJson(ResumeReviewPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapReview(parseReviewJson(result.text()), result.providerLabel(), false);
    }

    public StudentResumeOptimizerResponseDTO optimizeResume(String email, StudentResumeOptimizerRequestDTO request) {
        requireConfigured();
        requireResumeText(request.getResumeText());
        validateOffreId(request.getOffreId());
        Etudiant etudiant = resolveStudent(email);
        Offre offre = loadOffre(request.getOffreId());

        String cacheKey = "student-optimize:" + offre.getIdOffre() + ":" + etudiant.getIdEtudiant()
                + ":" + hash(request.getResumeText());
        if (!request.isForceRefresh()) {
            Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                return wrapOptimizer(parseOptimizerJson(cached.get().payload()), cached.get().provider(), true, offre);
            }
        }

        String userPrompt = ResumeOptimizerPrompt.user(truncateForAi(request.getResumeText()), buildJobDescription(offre));
        JobsLlmClient.LlmResult result = completeForJson(ResumeOptimizerPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapOptimizer(parseOptimizerJson(result.text()), result.providerLabel(), false, offre);
    }

    public StudentCoverLetterResponseDTO generateCoverLetter(String email, StudentCoverLetterRequestDTO request) {
        requireConfigured();
        Etudiant etudiant = resolveStudent(email);
        Offre offre = null;
        if (request.getOffreId() != null) {
            offre = loadOffre(request.getOffreId());
        }

        String jobTitle = request.getJobTitle();
        String companyName = request.getCompanyName();
        if (offre != null) {
            if (!StringUtils.hasText(jobTitle)) jobTitle = offre.getTitre();
            if (!StringUtils.hasText(companyName) && offre.getEntreprise() != null) {
                companyName = offre.getEntreprise().getNom();
            }
        }

        String cacheKey = "student-cover:" + etudiant.getIdEtudiant() + ":" + hash(jobTitle, companyName, request.getTemplateStyle());
        if (!request.isForceRefresh()) {
            Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                return wrapCoverLetter(parseCoverLetterJson(cached.get().payload()), cached.get().provider(), true);
            }
        }

        String userPrompt = CoverLetterPrompt.user(
                buildStudentProfile(etudiant),
                jobTitle,
                companyName,
                request.getTemplateStyle(),
                request.getAdditionalNotes()
        );
        JobsLlmClient.LlmResult result = llmClient.complete(CoverLetterPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapCoverLetter(parseCoverLetterJson(result.text()), result.providerLabel(), false);
    }

    public StudentInterviewPrepResponseDTO interviewPreparation(String email, StudentInterviewPrepRequestDTO request) {
        requireConfigured();
        Etudiant etudiant = resolveStudent(email);
        Offre offre = null;
        String jobTitle = request.getJobTitle();
        String jobDescription = request.getJobDescription();

        if (request.getOffreId() != null) {
            offre = loadOffre(request.getOffreId());
            if (!StringUtils.hasText(jobTitle)) jobTitle = offre.getTitre();
            if (!StringUtils.hasText(jobDescription)) jobDescription = buildJobDescription(offre);
        }
        if (!StringUtils.hasText(jobTitle)) {
            throw new IllegalArgumentException("jobTitle or offreId is required");
        }
        if (!StringUtils.hasText(jobDescription)) {
            jobDescription = jobTitle;
        }

        String cacheKey = "student-interview:" + etudiant.getIdEtudiant() + ":" + hash(jobTitle, jobDescription);
        if (!request.isForceRefresh()) {
            Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                return wrapInterview(parseInterviewJson(cached.get().payload()), cached.get().provider(), true);
            }
        }

        String userPrompt = InterviewPrepPrompt.user(jobTitle, jobDescription, buildStudentProfile(etudiant));
        JobsLlmClient.LlmResult result = llmClient.complete(InterviewPrepPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapInterview(parseInterviewJson(result.text()), result.providerLabel(), false);
    }

    public StudentApplicationOptimizerResponseDTO applicationOptimizer(String email, StudentApplicationOptimizerRequestDTO request) {
        requireConfigured();
        requireResumeText(request.getResumeText());
        validateOffreId(request.getOffreId());
        Etudiant etudiant = resolveStudent(email);
        Offre offre = loadOffre(request.getOffreId());

        String cacheKey = "student-app-opt:" + offre.getIdOffre() + ":" + etudiant.getIdEtudiant()
                + ":" + hash(request.getResumeText());
        if (!request.isForceRefresh()) {
            Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                return wrapAppOptimizer(parseAppOptimizerJson(cached.get().payload()), cached.get().provider(), true, offre);
            }
        }

        String userPrompt = ApplicationOptimizerPrompt.user(
                buildStudentProfile(etudiant),
                truncateForAi(request.getResumeText()),
                buildJobDescription(offre)
        );
        JobsLlmClient.LlmResult result = completeForJson(ApplicationOptimizerPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapAppOptimizer(parseAppOptimizerJson(result.text()), result.providerLabel(), false, offre);
    }

    public StudentCareerAdviceResponseDTO careerAdvice(String email, StudentCareerAdviceRequestDTO request) {
        requireConfigured();
        if (request == null || !StringUtils.hasText(request.getQuestion())) {
            throw new IllegalArgumentException("question is required");
        }
        Etudiant etudiant = resolveStudent(email);

        String cacheKey = "student-advice:" + etudiant.getIdEtudiant() + ":" + hash(request.getQuestion());
        if (!request.isForceRefresh()) {
            Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                return wrapAdvice(parseAdviceJson(cached.get().payload()), cached.get().provider(), true);
            }
        }

        String userPrompt = CareerAdvicePrompt.user(buildStudentProfile(etudiant), request.getQuestion().trim());
        JobsLlmClient.LlmResult result = llmClient.complete(CareerAdvicePrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapAdvice(parseAdviceJson(result.text()), result.providerLabel(), false);
    }

    private Etudiant resolveStudent(String email) {
        return studentAccountResolver.resolveOrProvision(email);
    }

    private Offre loadOffre(Long offreId) {
        return offreRepository.findById(offreId)
                .orElseThrow(() -> new NotFoundException("Offre introuvable avec id: " + offreId));
    }

    private void validateOffreId(Long offreId) {
        if (offreId == null) {
            throw new IllegalArgumentException("offreId is required");
        }
    }

    private void requireResumeText(String resumeText) {
        if (!StringUtils.hasText(resumeText)) {
            throw new IllegalArgumentException("resumeText is required");
        }
    }

    private void requireConfigured() {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("AI is not configured. Set GEMINI_API_KEY or OPENAI_API_KEY.");
        }
    }

    private String buildStudentProfile(Etudiant etudiant) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(etudiant.getNom()).append("\n");
        sb.append("Email: ").append(etudiant.getEmail()).append("\n");
        sb.append("Filiere: ").append(nullSafe(etudiant.getFiliere())).append("\n");
        sb.append("Niveau: ").append(etudiant.getNiveau()).append("\n");
        if (etudiant.getCompetences() != null && !etudiant.getCompetences().isEmpty()) {
            sb.append("Skills: ").append(etudiant.getCompetences().stream()
                    .map(Competence::getLibelle).collect(Collectors.joining(", "))).append("\n");
        }
        if (etudiant.getProfil() != null) {
            if (StringUtils.hasText(etudiant.getProfil().getBio())) {
                sb.append("Bio/Experience: ").append(etudiant.getProfil().getBio()).append("\n");
            }
            if (StringUtils.hasText(etudiant.getProfil().getSiteWeb())) {
                sb.append("Portfolio: ").append(etudiant.getProfil().getSiteWeb()).append("\n");
            }
            if (StringUtils.hasText(etudiant.getProfil().getLienLinkedIn())) {
                sb.append("LinkedIn: ").append(etudiant.getProfil().getLienLinkedIn()).append("\n");
            }
            if (StringUtils.hasText(etudiant.getProfil().getLienGitHub())) {
                sb.append("GitHub: ").append(etudiant.getProfil().getLienGitHub()).append("\n");
            }
        }
        return sb.toString();
    }

    private String buildJobDescription(Offre offre) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(offre.getTitre()).append("\n");
        sb.append("Department: ").append(nullSafe(offre.getDepartment())).append("\n");
        sb.append("Domain: ").append(nullSafe(offre.getDomaine())).append("\n");
        sb.append("Experience: ").append(offre.getExperienceLevel()).append("\n");
        sb.append("Work Mode: ").append(offre.getWorkMode()).append("\n");
        sb.append("Location: ").append(nullSafe(offre.getLocalisation())).append("\n");
        sb.append("Contract: ").append(offre.getTypeOffre()).append("\n");
        sb.append("Description: ").append(nullSafe(offre.getDescription())).append("\n");
        sb.append("Responsibilities: ").append(nullSafe(offre.getResponsibilities())).append("\n");
        sb.append("Requirements: ").append(nullSafe(offre.getRequirements())).append("\n");
        if (offre.getCompetencesRequises() != null) {
            sb.append("Required Skills: ").append(String.join(", ", offre.getCompetencesRequises())).append("\n");
        }
        if (offre.getTechnologies() != null) {
            sb.append("Technologies: ").append(String.join(", ", offre.getTechnologies())).append("\n");
        }
        return sb.toString();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String truncateForAi(String text) {
        if (text == null) return "";
        if (text.length() <= MAX_RESUME_CHARS) return text;
        return text.substring(0, MAX_RESUME_CHARS) + "\n[...truncated for AI analysis]";
    }

    private JobsLlmClient.LlmResult completeForJson(String systemPrompt, String userPrompt) {
        JobsLlmClient.LlmResult result = llmClient.complete(systemPrompt, userPrompt, JSON_MAX_TOKENS);
        try {
            JobsAiJsonParser.parse(result.text());
            return result;
        } catch (IllegalStateException ex) {
            log.warn("AI JSON invalid after first attempt, retrying: {}", ex.getMessage());
            String retryUser = userPrompt + """

                    Return complete valid JSON only. Max 4 items per array. Each string max 80 characters.
                    """;
            return llmClient.complete(systemPrompt, retryUser, JSON_MAX_TOKENS);
        }
    }

    private JsonNode parseJson(String raw) {
        return JobsAiJsonParser.parse(raw);
    }

    private List<String> readStringList(JsonNode node) {
        if (!node.isArray()) return List.of();
        List<String> list = new ArrayList<>();
        node.forEach(n -> list.add(n.asText()));
        return list;
    }

    private StudentJobMatchResponseDTO parseMatchJson(String json) {
        try {
            JsonNode node = parseJson(json);
            StudentJobMatchResponseDTO dto = new StudentJobMatchResponseDTO();
            dto.setOverallScore(node.path("overallScore").asInt(0));
            dto.setSkillsScore(node.path("skillsScore").asInt(0));
            dto.setProjectsScore(node.path("projectsScore").asInt(0));
            dto.setExperienceScore(node.path("experienceScore").asInt(0));
            dto.setEducationScore(node.path("educationScore").asInt(0));
            dto.setMatchingSkills(readStringList(node.path("matchingSkills")));
            dto.setMissingSkills(readStringList(node.path("missingSkills")));
            dto.setImprovementSuggestions(readStringList(node.path("improvementSuggestions")));
            dto.setRecommendation(node.path("recommendation").asText(""));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI job match response: " + e.getMessage(), e);
        }
    }

    private StudentResumeReviewResponseDTO parseReviewJson(String json) {
        try {
            JsonNode node = parseJson(json);
            StudentResumeReviewResponseDTO dto = new StudentResumeReviewResponseDTO();
            dto.setAtsScore(node.path("atsScore").asInt(0));
            dto.setAtsLabel(node.path("atsLabel").asText("Score"));
            dto.setStrengths(readStringList(node.path("strengths")));
            dto.setWeaknesses(readStringList(node.path("weaknesses")));
            dto.setFormattingIssues(readStringList(node.path("formattingIssues")));
            dto.setKeywordGaps(readStringList(node.path("keywordGaps")));
            dto.setSuggestions(readStringList(node.path("suggestions")));
            dto.setExtractedSkills(readStringList(node.path("extractedSkills")));
            dto.setExtractedExperience(readStringList(node.path("extractedExperience")));
            dto.setExtractedEducation(readStringList(node.path("extractedEducation")));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI resume review response: " + e.getMessage(), e);
        }
    }

    private StudentResumeOptimizerResponseDTO parseOptimizerJson(String json) {
        try {
            JsonNode node = parseJson(json);
            StudentResumeOptimizerResponseDTO dto = new StudentResumeOptimizerResponseDTO();
            dto.setAtsScore(node.path("atsScore").asInt(0));
            dto.setAtsLabel(node.path("atsLabel").asText("Score"));
            dto.setOptimizedSummary(node.path("optimizedSummary").asText(""));
            dto.setImprovedBulletPoints(readStringList(node.path("improvedBulletPoints")));
            dto.setAtsKeywords(readStringList(node.path("atsKeywords")));
            dto.setMissingSkills(readStringList(node.path("missingSkills")));
            dto.setSuggestions(readStringList(node.path("suggestions")));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI resume optimizer response: " + e.getMessage(), e);
        }
    }

    private StudentCoverLetterResponseDTO parseCoverLetterJson(String json) {
        try {
            JsonNode node = parseJson(json);
            StudentCoverLetterResponseDTO dto = new StudentCoverLetterResponseDTO();
            dto.setLetter(node.path("letter").asText(""));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI cover letter response: " + e.getMessage(), e);
        }
    }

    private StudentInterviewPrepResponseDTO parseInterviewJson(String json) {
        try {
            JsonNode node = parseJson(json);
            StudentInterviewPrepResponseDTO dto = new StudentInterviewPrepResponseDTO();
            dto.setTechnicalQuestions(readStringList(node.path("technicalQuestions")));
            dto.setBehavioralQuestions(readStringList(node.path("behavioralQuestions")));
            dto.setHrQuestions(readStringList(node.path("hrQuestions")));
            dto.setSuggestedAnswers(readStringList(node.path("suggestedAnswers")));
            dto.setInterviewTips(readStringList(node.path("interviewTips")));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI interview prep response: " + e.getMessage(), e);
        }
    }

    private StudentApplicationOptimizerResponseDTO parseAppOptimizerJson(String json) {
        try {
            JsonNode node = parseJson(json);
            StudentApplicationOptimizerResponseDTO dto = new StudentApplicationOptimizerResponseDTO();
            dto.setMatchScore(node.path("matchScore").asInt(0));
            dto.setMatchLabel(node.path("matchLabel").asText(""));
            dto.setSkillsScore(node.path("skillsScore").asInt(0));
            dto.setReadinessScore(node.path("readinessScore").asInt(0));
            dto.setMissingSkills(readStringList(node.path("missingSkills")));
            dto.setOptimizedSummary(node.path("optimizedSummary").asText(""));
            dto.setImprovedBulletPoints(readStringList(node.path("improvedBulletPoints")));
            dto.setCoverLetter(node.path("coverLetter").asText(""));
            dto.setAtsKeywords(readStringList(node.path("atsKeywords")));
            dto.setReadinessChecklist(readStringList(node.path("readinessChecklist")));
            dto.setRecommendation(node.path("recommendation").asText(""));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI application optimizer response: " + e.getMessage(), e);
        }
    }

    private StudentCareerAdviceResponseDTO parseAdviceJson(String json) {
        try {
            JsonNode node = parseJson(json);
            StudentCareerAdviceResponseDTO dto = new StudentCareerAdviceResponseDTO();
            dto.setAnswer(node.path("answer").asText(""));
            dto.setActionItems(readStringList(node.path("actionItems")));
            dto.setResources(readStringList(node.path("resources")));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI career advice response: " + e.getMessage(), e);
        }
    }

    private StudentJobMatchResponseDTO wrapMatch(StudentJobMatchResponseDTO dto, String provider, boolean cached, Offre offre) {
        dto.setProvider(provider);
        dto.setCached(cached);
        dto.setAiDisclaimer("AI matching score. Use as guidance alongside your own judgment.");
        dto.setOffreId(offre.getIdOffre());
        dto.setJobTitle(offre.getTitre());
        return dto;
    }

    private StudentResumeReviewResponseDTO wrapReview(StudentResumeReviewResponseDTO dto, String provider, boolean cached) {
        dto.setProvider(provider);
        dto.setCached(cached);
        dto.setAiDisclaimer("AI resume analysis. Review suggestions before applying changes.");
        return dto;
    }

    private StudentResumeOptimizerResponseDTO wrapOptimizer(StudentResumeOptimizerResponseDTO dto, String provider, boolean cached, Offre offre) {
        dto.setProvider(provider);
        dto.setCached(cached);
        dto.setAiDisclaimer("AI-optimized resume suggestions. Tailor before submitting.");
        dto.setOffreId(offre.getIdOffre());
        dto.setJobTitle(offre.getTitre());
        return dto;
    }

    private StudentCoverLetterResponseDTO wrapCoverLetter(StudentCoverLetterResponseDTO dto, String provider, boolean cached) {
        dto.setProvider(provider);
        dto.setCached(cached);
        dto.setAiDisclaimer("AI-generated cover letter. Personalize before sending.");
        return dto;
    }

    private StudentInterviewPrepResponseDTO wrapInterview(StudentInterviewPrepResponseDTO dto, String provider, boolean cached) {
        dto.setProvider(provider);
        dto.setCached(cached);
        dto.setAiDisclaimer("AI-generated interview preparation. Practice with real examples from your experience.");
        return dto;
    }

    private StudentApplicationOptimizerResponseDTO wrapAppOptimizer(StudentApplicationOptimizerResponseDTO dto, String provider, boolean cached, Offre offre) {
        dto.setProvider(provider);
        dto.setCached(cached);
        dto.setAiDisclaimer("AI application package optimization. Review all content before applying.");
        dto.setOffreId(offre.getIdOffre());
        dto.setJobTitle(offre.getTitre());
        return dto;
    }

    private StudentCareerAdviceResponseDTO wrapAdvice(StudentCareerAdviceResponseDTO dto, String provider, boolean cached) {
        dto.setProvider(provider);
        dto.setCached(cached);
        dto.setAiDisclaimer("AI career guidance. Consult mentors for important decisions.");
        return dto;
    }

    private String hash(Object... parts) {
        try {
            String combined = Arrays.stream(parts).map(p -> p == null ? "" : p.toString()).collect(Collectors.joining("|"));
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}
