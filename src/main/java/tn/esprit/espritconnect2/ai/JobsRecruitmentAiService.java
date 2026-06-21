package tn.esprit.espritconnect2.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tn.esprit.espritconnect2.DTO.*;
import tn.esprit.espritconnect2.Entitie.Candidature;
import tn.esprit.espritconnect2.Entitie.Competence;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Offre;
import tn.esprit.espritconnect2.Repository.CandidatureRepository;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.OffreRepository;
import tn.esprit.espritconnect2.ai.prompts.*;
import tn.esprit.espritconnect2.exception.NotFoundException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobsRecruitmentAiService {

    private final JobsAiProperties properties;
    private final JobsLlmClient llmClient;
    private final JobsAiCache cache;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OffreRepository offreRepository;
    private final EtudiantRepository etudiantRepository;
    private final CandidatureRepository candidatureRepository;

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public AiJobGenerateWrapperDTO generateJob(AIJobGenerateRequestDTO request) {
        validateGenerateRequest(request);
        String lang = normalizeLanguage(request.getOutputLanguage());
        String cacheKey = "generate:" + hash(request);

        Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            return wrapGenerate(parseGenerateJson(cached.get().payload()), cached.get().provider(), true, lang);
        }

        String skills = request.getSkills() != null ? String.join(", ", request.getSkills()) : "";
        String userPrompt = GenerateJobDescriptionPrompt.user(
                request.getTitle(),
                skills,
                request.getExperienceLevel(),
                request.getContractType(),
                request.getDepartment(),
                request.getLocation(),
                lang,
                request.getAdditionalPrompt()
        );

        JobsLlmClient.LlmResult result = llmClient.complete(GenerateJobDescriptionPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapGenerate(parseGenerateJson(result.text()), result.providerLabel(), false, lang);
    }

    public AiJobGenerateWrapperDTO improveJob(AIImproveTextRequestDTO request) {
        if (request == null || !StringUtils.hasText(request.getOriginalText())) {
            throw new IllegalArgumentException("Original text is required");
        }
        String lang = normalizeLanguage(request.getOutputLanguage());
        String cacheKey = "improve:" + hash(request.getOriginalText(), request.getJobTitle(), lang);

        Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            return wrapGenerate(parseGenerateJson(cached.get().payload()), cached.get().provider(), true, lang);
        }

        String userPrompt = ImproveJobDescriptionPrompt.user(
                request.getOriginalText().trim(),
                request.getJobTitle(),
                lang
        );
        JobsLlmClient.LlmResult result = llmClient.complete(ImproveJobDescriptionPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapGenerate(parseGenerateJson(result.text()), result.providerLabel(), false, lang);
    }

    public AiImportExtractResponseDTO extractImport(AiImportExtractRequestDTO request) {
        if (request == null || !StringUtils.hasText(request.getRawContent())) {
            throw new IllegalArgumentException("Raw content is required");
        }
        String source = request.getSource() != null ? request.getSource() : "TEXT";
        String cacheKey = "import:" + hash(source, request.getRawContent());

        if (!request.isForceRefresh()) {
            Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                return wrapImport(parseImportJson(cached.get().payload()), cached.get().provider(), true);
            }
        }

        String userPrompt = JobImportExtractPrompt.user(request.getRawContent(), source);
        JobsLlmClient.LlmResult result = llmClient.complete(JobImportExtractPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapImport(parseImportJson(result.text()), result.providerLabel(), false);
    }

    public AiMatchCandidateResponseDTO matchCandidate(AiMatchCandidateRequestDTO request) {
        if (request == null || request.getOffreId() == null) {
            throw new IllegalArgumentException("offreId is required");
        }

        Offre offre = offreRepository.findById(request.getOffreId())
                .orElseThrow(() -> new NotFoundException("Offre introuvable avec id: " + request.getOffreId()));

        Etudiant etudiant = resolveEtudiant(request);
        String cacheKey = "match:" + offre.getIdOffre() + ":" + etudiant.getIdEtudiant();

        if (!request.isForceRefresh()) {
            Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                AiMatchCandidateResponseDTO dto = parseMatchJson(cached.get().payload());
                enrichMatchMeta(dto, etudiant, offre, cached.get().provider(), true);
                return dto;
            }
        }

        String userPrompt = CandidateMatchingPrompt.user(buildJobProfile(offre), buildCandidateProfile(etudiant, request.getCandidatureId()));
        JobsLlmClient.LlmResult result = llmClient.complete(CandidateMatchingPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());

        AiMatchCandidateResponseDTO dto = parseMatchJson(result.text());
        enrichMatchMeta(dto, etudiant, offre, result.providerLabel(), false);
        return dto;
    }

    public AiCandidateSummaryResponseDTO candidateSummary(AiCandidateSummaryRequestDTO request) {
        if (request == null || request.getOffreId() == null) {
            throw new IllegalArgumentException("offreId is required");
        }

        Offre offre = offreRepository.findById(request.getOffreId())
                .orElseThrow(() -> new NotFoundException("Offre introuvable avec id: " + request.getOffreId()));

        AiMatchCandidateResponseDTO matchData = request.getMatchData();
        if (matchData == null) {
            AiMatchCandidateRequestDTO matchReq = new AiMatchCandidateRequestDTO();
            matchReq.setOffreId(request.getOffreId());
            matchReq.setEtudiantId(request.getEtudiantId());
            matchReq.setCandidatureId(request.getCandidatureId());
            matchReq.setForceRefresh(request.isForceRefresh());
            matchData = matchCandidate(matchReq);
        }

        Long etudiantId = matchData.getEtudiantId() != null ? matchData.getEtudiantId() : request.getEtudiantId();
        String cacheKey = "summary:" + offre.getIdOffre() + ":" + etudiantId;

        if (!request.isForceRefresh()) {
            Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                return wrapSummary(parseSummaryJson(cached.get().payload()), cached.get().provider(), true);
            }
        }

        String matchJson;
        try {
            matchJson = objectMapper.writeValueAsString(matchData);
        } catch (Exception e) {
            matchJson = matchData.toString();
        }

        String userPrompt = CandidateSummaryPrompt.user(offre.getTitre(), matchJson);
        JobsLlmClient.LlmResult result = llmClient.complete(CandidateSummaryPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapSummary(parseSummaryJson(result.text()), result.providerLabel(), false);
    }

    public AiRecruitmentInsightsResponseDTO recruitmentInsights(AiRecruitmentInsightsRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        String cacheKey = "insights:" + hash(request);

        if (!request.isForceRefresh()) {
            Optional<JobsAiCache.CachedResult> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                return wrapInsights(parseInsightsJson(cached.get().payload()), cached.get().provider(), true);
            }
        }

        String analyticsData = buildAnalyticsPayload(request);
        String userPrompt = RecruitmentInsightsPrompt.user(analyticsData);
        JobsLlmClient.LlmResult result = llmClient.complete(RecruitmentInsightsPrompt.SYSTEM, userPrompt);
        cache.put(cacheKey, result.text(), result.providerLabel(), properties.getCacheTtlMinutes());
        return wrapInsights(parseInsightsJson(result.text()), result.providerLabel(), false);
    }

    /** Backward-compatible response for JobAIService / offres/ai endpoints */
    public AIJobGenerateResponseDTO generateJobDescription(AIJobGenerateRequestDTO request) {
        return generateJob(request).getData();
    }

    public AIJobGenerateResponseDTO improveJobDescription(AIImproveTextRequestDTO request) {
        return improveJob(request).getData();
    }

    private void validateGenerateRequest(AIJobGenerateRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        boolean hasPrompt = StringUtils.hasText(request.getAdditionalPrompt());
        boolean hasStructured = StringUtils.hasText(request.getTitle());
        if (!hasPrompt && !hasStructured) {
            throw new IllegalArgumentException("Provide a job title or additional prompt");
        }
    }

    private Etudiant resolveEtudiant(AiMatchCandidateRequestDTO request) {
        if (request.getCandidatureId() != null) {
            Candidature c = candidatureRepository.findById(request.getCandidatureId())
                    .orElseThrow(() -> new NotFoundException("Candidature introuvable"));
            if (c.getEtudiant() == null) {
                throw new NotFoundException("Candidature has no student");
            }
            return c.getEtudiant();
        }
        if (request.getEtudiantId() != null) {
            return etudiantRepository.findById(request.getEtudiantId())
                    .orElseThrow(() -> new NotFoundException("Etudiant introuvable"));
        }
        throw new IllegalArgumentException("etudiantId or candidatureId is required");
    }

    private String buildJobProfile(Offre offre) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(offre.getTitre()).append("\n");
        sb.append("Department: ").append(nullSafe(offre.getDepartment())).append("\n");
        sb.append("Experience: ").append(offre.getExperienceLevel()).append("\n");
        sb.append("Location: ").append(nullSafe(offre.getLocalisation())).append("\n");
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

    private String buildCandidateProfile(Etudiant etudiant, Long candidatureId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(etudiant.getNom()).append("\n");
        sb.append("Email: ").append(etudiant.getEmail()).append("\n");
        sb.append("Filiere: ").append(nullSafe(etudiant.getFiliere())).append("\n");
        sb.append("Niveau: ").append(etudiant.getNiveau()).append("\n");
        if (etudiant.getCompetences() != null) {
            sb.append("Skills: ").append(etudiant.getCompetences().stream()
                    .map(Competence::getLibelle).collect(Collectors.joining(", "))).append("\n");
        }
        if (etudiant.getProfil() != null && StringUtils.hasText(etudiant.getProfil().getBio())) {
            sb.append("Bio/Experience: ").append(etudiant.getProfil().getBio()).append("\n");
        }
        Candidature candidature = resolveCandidature(etudiant, candidatureId);
        if (candidature != null) {
            sb.append("Cover Letter: ").append(nullSafe(candidature.getLettreMotivation())).append("\n");
            sb.append("Has Resume: ").append(candidature.getFichier() != null).append("\n");
        }
        return sb.toString();
    }

    private Candidature resolveCandidature(Etudiant etudiant, Long candidatureId) {
        if (candidatureId != null) {
            return candidatureRepository.findById(candidatureId).orElse(null);
        }
        if (etudiant.getCandidatures() != null && !etudiant.getCandidatures().isEmpty()) {
            return etudiant.getCandidatures().get(0);
        }
        return null;
    }

    private String buildAnalyticsPayload(AiRecruitmentInsightsRequestDTO request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Total Offers: ").append(request.getTotalOffers()).append("\n");
        sb.append("Active Offers: ").append(request.getActiveOffers()).append("\n");
        sb.append("Total Applications: ").append(request.getTotalApplications()).append("\n");
        sb.append("Avg Applications/Offer: ").append(request.getAvgApplicationsPerOffer()).append("\n");
        if (request.getApplicationsByStatus() != null) {
            sb.append("Applications by Status: ").append(request.getApplicationsByStatus()).append("\n");
        }
        if (request.getTopCandidateScores() != null) {
            sb.append("Top Candidate Scores: ").append(request.getTopCandidateScores()).append("\n");
        }
        if (request.getCommonMissingSkills() != null) {
            sb.append("Common Missing Skills: ").append(request.getCommonMissingSkills()).append("\n");
        }
        if (request.getOffreId() != null) {
            sb.append("Focused Offer ID: ").append(request.getOffreId()).append("\n");
        }
        return sb.toString();
    }

    private AIJobGenerateResponseDTO parseGenerateJson(String json) {
        try {
            JsonNode node = parseJson(json);
            AIJobGenerateResponseDTO dto = new AIJobGenerateResponseDTO();
            dto.setDescription(node.path("description").asText(""));
            dto.setResponsibilities(node.path("responsibilities").asText(""));
            dto.setRequirements(node.path("requirements").asText(""));
            dto.setBenefits(node.path("benefits").asText(""));
            dto.setRecruitmentText(node.path("recruitmentText").asText(""));
            dto.setSuggestedTitle(node.path("suggestedTitle").asText(null));
            dto.setKeywords(readStringList(node.path("keywords")));
            dto.setSuggestedSkills(readStringList(node.path("suggestedSkills")));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI job response: " + e.getMessage(), e);
        }
    }

    private AiImportExtractResponseDTO parseImportJson(String json) {
        try {
            JsonNode node = parseJson(json);
            AiImportExtractResponseDTO dto = new AiImportExtractResponseDTO();
            dto.setTitle(node.path("title").asText(""));
            dto.setContractType(node.path("contractType").asText(""));
            dto.setExperienceLevel(node.path("experienceLevel").asText(""));
            dto.setLocation(node.path("location").asText(""));
            dto.setSkills(readStringList(node.path("skills")));
            dto.setResponsibilities(node.path("responsibilities").asText(""));
            dto.setRequirements(node.path("requirements").asText(""));
            dto.setBenefits(node.path("benefits").asText(""));
            dto.setDescription(node.path("description").asText(""));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI import response: " + e.getMessage(), e);
        }
    }

    private AiMatchCandidateResponseDTO parseMatchJson(String json) {
        try {
            JsonNode node = parseJson(json);
            AiMatchCandidateResponseDTO dto = new AiMatchCandidateResponseDTO();
            dto.setOverallScore(node.path("overallScore").asInt(0));
            dto.setSkillsScore(node.path("skillsScore").asInt(0));
            dto.setExperienceScore(node.path("experienceScore").asInt(0));
            dto.setEducationScore(node.path("educationScore").asInt(0));
            dto.setMatchingSkills(readStringList(node.path("matchingSkills")));
            dto.setMissingSkills(readStringList(node.path("missingSkills")));
            dto.setStrengths(readStringList(node.path("strengths")));
            dto.setWeaknesses(readStringList(node.path("weaknesses")));
            dto.setRecommendation(node.path("recommendation").asText(""));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI match response: " + e.getMessage(), e);
        }
    }

    private AiCandidateSummaryResponseDTO parseSummaryJson(String json) {
        try {
            JsonNode node = parseJson(json);
            AiCandidateSummaryResponseDTO dto = new AiCandidateSummaryResponseDTO();
            dto.setSummary(node.path("summary").asText(""));
            dto.setOverallScore(node.path("overallScore").asInt(0));
            dto.setRecommendation(node.path("recommendation").asText(""));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI summary response: " + e.getMessage(), e);
        }
    }

    private AiRecruitmentInsightsResponseDTO parseInsightsJson(String json) {
        try {
            JsonNode node = parseJson(json);
            AiRecruitmentInsightsResponseDTO dto = new AiRecruitmentInsightsResponseDTO();
            dto.setInsights(readStringList(node.path("insights")));
            dto.setSummary(node.path("summary").asText(""));
            dto.setRecommendations(readStringList(node.path("recommendations")));
            return dto;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse AI insights response: " + e.getMessage(), e);
        }
    }

    private JsonNode parseJson(String raw) throws Exception {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("(?s)^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
        }
        return objectMapper.readTree(cleaned);
    }

    private List<String> readStringList(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }
        List<String> list = new ArrayList<>();
        node.forEach(n -> list.add(n.asText()));
        return list;
    }

    private AiJobGenerateWrapperDTO wrapGenerate(AIJobGenerateResponseDTO data, String provider, boolean cached, String lang) {
        data.setAiDisclaimer(localizedDisclaimer(lang));
        AiJobGenerateWrapperDTO wrapper = new AiJobGenerateWrapperDTO();
        wrapper.setData(data);
        wrapper.setProvider(provider);
        wrapper.setCached(cached);
        wrapper.setAiDisclaimer(data.getAiDisclaimer());
        return wrapper;
    }

    private AiImportExtractResponseDTO wrapImport(AiImportExtractResponseDTO data, String provider, boolean cached) {
        data.setProvider(provider);
        data.setCached(cached);
        data.setAiDisclaimer("Extracted by AI. Please review before publishing.");
        return data;
    }

    private void enrichMatchMeta(AiMatchCandidateResponseDTO dto, Etudiant etudiant, Offre offre,
                                 String provider, boolean cached) {
        dto.setEtudiantId(etudiant.getIdEtudiant());
        dto.setOffreId(offre.getIdOffre());
        dto.setEtudiantNom(etudiant.getNom());
        dto.setProvider(provider);
        dto.setCached(cached);
        dto.setAiDisclaimer("AI matching score. Use as guidance alongside human review.");
    }

    private AiCandidateSummaryResponseDTO wrapSummary(AiCandidateSummaryResponseDTO data, String provider, boolean cached) {
        data.setProvider(provider);
        data.setCached(cached);
        data.setAiDisclaimer("AI-generated candidate summary for recruiter review.");
        return data;
    }

    private AiRecruitmentInsightsResponseDTO wrapInsights(AiRecruitmentInsightsResponseDTO data, String provider, boolean cached) {
        data.setProvider(provider);
        data.setCached(cached);
        data.setAiDisclaimer("AI-generated recruitment insights based on current dashboard data.");
        return data;
    }

    private String normalizeLanguage(String language) {
        if (!StringUtils.hasText(language)) return "English";
        return switch (language.trim().toLowerCase(Locale.ROOT)) {
            case "fr" -> "French";
            case "ar" -> "Arabic";
            default -> "English";
        };
    }

    private String localizedDisclaimer(String lang) {
        return switch (lang) {
            case "French" -> "Contenu généré par IA. Veuillez relire avant publication.";
            case "Arabic" -> "تم إنشاء هذا المحتوى بواسطة الذكاء الاصطناعي. يرجى مراجعته قبل النشر.";
            default -> "AI-generated content. Please review before publishing.";
        };
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
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
