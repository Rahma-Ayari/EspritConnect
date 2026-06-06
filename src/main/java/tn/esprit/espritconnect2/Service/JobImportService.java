package tn.esprit.espritconnect2.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;
import tn.esprit.espritconnect2.DTO.ImportJobResponseDTO;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class JobImportService {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern SCRIPT_STYLE_PATTERN = Pattern.compile(
            "(?is)<(script|style|noscript)[^>]*>.*?</\\1>"
    );
    private static final Pattern JSON_LD_SCRIPT_PATTERN = Pattern.compile(
            "(?is)<script[^>]*type=[\"']application/ld\\+json[\"'][^>]*>(.*?)</script>"
    );
    private static final Pattern SKILL_PATTERN = Pattern.compile(
            "(?i)(?:skills?|technologies?|competences?|requirements?)\\s*[:\\-]\\s*(.+)"
    );
    private static final Pattern LINKEDIN_TITLE_PATTERN = Pattern.compile(
            "(?i)^(.+?)\\s+in\\s+[A-Za-zÀ-ÿ\\s,.'-]+\\s*\\|\\s*LinkedIn"
    );
    private static final Pattern INLINE_DESCRIPTION_PATTERN = Pattern.compile(
            "\"description\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\]){120,})\""
    );
    private static final Pattern INLINE_DESCRIPTION_TEXT_PATTERN = Pattern.compile(
            "\"description\"\\s*:\\s*\\{\\s*\"text\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])+)\""
    );
    private static final Pattern JOB_POSTING_DESCRIPTION_PATTERN = Pattern.compile(
            "\"jobPostingDescription\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\]){120,})\""
    );
    private static final int MIN_JOB_DESCRIPTION_LENGTH = 120;
    private static final List<String> PAGE_NOISE_MARKERS = List.of(
            "Similar jobs",
            "People also viewed",
            "Sign in to evaluate",
            "Sign in to tailor",
            "Skip to main content",
            "Join now",
            "Agree & Join LinkedIn",
            "Explore top content on LinkedIn",
            "Get notified when a new job is posted",
            "Referrals increase your chances",
            "function getDfd"
    );

    private final RestClient restClient = RestClient.builder()
            .defaultHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                            + "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .defaultHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .defaultHeader("Accept-Language", "en-US,en;q=0.9,fr;q=0.8")
            .build();

    public ImportJobResponseDTO importFromText(String text, String source) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Job text is required for import");
        }

        String normalized = normalizeImportText(text);
        if (looksLikeLinkedInTitleOnly(normalized.replace("\n", " "))) {
            throw new IllegalArgumentException(
                    "Could not extract a job description from this text. "
                            + "Paste the full job description (not only the page title).");
        }
        JobSections sections = parseJobSections(normalized);
        ImportJobResponseDTO response = new ImportJobResponseDTO();
        response.setTitle(extractTitle(normalized));
        response.setDescription(sections.description);
        response.setResponsibilities(sections.responsibilities);
        response.setRequirements(sections.requirements);
        response.setSkills(extractSkills(normalized));
        response.setLocation(extractLocation(normalized));
        response.setContractType(guessContractType(normalized));
        response.setExtractedData(new HashMap<>(Map.of(
                "source", source != null ? source : "TEXT",
                "lineCount", normalized.split("\n").length
        )));
        return response;
    }

    public ImportJobResponseDTO importFromUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL is required for import");
        }

        try {
            String html = restClient.get()
                    .uri(URI.create(url.trim()))
                    .retrieve()
                    .body(String.class);

            if (html == null || html.isBlank()) {
                throw new IllegalArgumentException("Could not fetch content from URL");
            }

            if (isLinkedInUrl(url)) {
                ImportJobResponseDTO linkedIn = tryParseLinkedIn(html);
                Map<String, Object> meta = new HashMap<>(
                        linkedIn.getExtractedData() != null ? linkedIn.getExtractedData() : Map.of());
                meta.put("source", "LINKEDIN");
                meta.put("url", url);
                linkedIn.setExtractedData(meta);
                return linkedIn;
            }

            String text = htmlToPlainText(html);
            ImportJobResponseDTO response = importFromText(text, "URL");
            Map<String, Object> meta = new HashMap<>(response.getExtractedData());
            meta.put("url", url);
            response.setExtractedData(meta);
            return response;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to import from URL: " + e.getMessage());
        }
    }

    private boolean isLinkedInUrl(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.contains("linkedin.com/jobs") || lower.contains("linkedin.com/job");
    }

    private ImportJobResponseDTO tryParseLinkedIn(String html) {
        Optional<ImportJobResponseDTO> fromJsonLd = parseLinkedInJsonLd(html);
        if (fromJsonLd.isPresent()) {
            return fromJsonLd.get();
        }

        String ogTitle = extractMetaContent(html, "og:title");
        String title = ogTitle != null ? cleanLinkedInOgTitle(ogTitle) : null;

        String description = firstNonBlank(
                decodeHtmlEntities(extractMetaContent(html, "og:description")),
                decodeHtmlEntities(extractMetaContent(html, "description")),
                decodeHtmlEntities(extractMetaContent(html, "twitter:description")),
                extractEmbeddedDescription(html)
        );

        if (description != null && !looksLikeLinkedInTitleOnly(description)) {
            String cleanDescription = normalizeImportText(description);
            if (cleanDescription.length() >= MIN_JOB_DESCRIPTION_LENGTH) {
                return buildLinkedInResponse(title, cleanDescription, "meta");
            }
        }

        String text = htmlToPlainText(html);
        Matcher titleMatcher = LINKEDIN_TITLE_PATTERN.matcher(text);
        if (titleMatcher.find()) {
            title = titleMatcher.group(1).trim();
        }

        if (!looksLikeLinkedInTitleOnly(text) && text.length() >= MIN_JOB_DESCRIPTION_LENGTH) {
            ImportJobResponseDTO response = importFromText(text, "LINKEDIN");
            if (title != null && !title.isBlank()) {
                response.setTitle(title);
            }
            response.getExtractedData().put("parser", "plain-text");
            return response;
        }

        if (isLinkedInLoginWall(html)) {
            throw new IllegalArgumentException(
                    "LinkedIn requires sign-in and did not return the full job description. "
                            + "Open the job on LinkedIn, copy the description, and use TEXT import.");
        }

        throw new IllegalArgumentException(
                "Could not extract the job description from this LinkedIn URL. "
                        + "Copy the full job text from LinkedIn and use TEXT import.");
    }

    private ImportJobResponseDTO buildLinkedInResponse(String title, String cleanDescription, String parser) {
        JobSections sections = parseJobSections(cleanDescription);
        ImportJobResponseDTO response = new ImportJobResponseDTO();
        response.setTitle(title != null && !title.isBlank()
                ? title
                : extractTitle(cleanDescription));
        response.setDescription(sections.description);
        response.setResponsibilities(sections.responsibilities);
        response.setRequirements(sections.requirements);
        response.setSkills(extractSkills(cleanDescription));
        response.setLocation(extractLocation(cleanDescription));
        response.setContractType(guessContractType(cleanDescription));
        response.setExtractedData(new HashMap<>(Map.of("parser", parser)));
        return response;
    }

    private String extractMetaContent(String html, String propertyOrName) {
        String quoted = Pattern.quote(propertyOrName);
        Pattern propertyFirst = Pattern.compile(
                "(?is)<meta[^>]*(?:property|name)\\s*=\\s*[\"']" + quoted + "[\"'][^>]*content\\s*=\\s*[\"']([^\"']*)[\"']"
        );
        Matcher m1 = propertyFirst.matcher(html);
        if (m1.find()) {
            return m1.group(1);
        }
        Pattern contentFirst = Pattern.compile(
                "(?is)<meta[^>]*content\\s*=\\s*[\"']([^\"']*)[\"'][^>]*(?:property|name)\\s*=\\s*[\"']" + quoted + "[\"']"
        );
        Matcher m2 = contentFirst.matcher(html);
        if (m2.find()) {
            return m2.group(1);
        }
        return null;
    }

    private String extractEmbeddedDescription(String html) {
        Matcher textObj = INLINE_DESCRIPTION_TEXT_PATTERN.matcher(html);
        if (textObj.find()) {
            return decodeJsonString(textObj.group(1));
        }
        Matcher jobDesc = JOB_POSTING_DESCRIPTION_PATTERN.matcher(html);
        if (jobDesc.find()) {
            return decodeJsonString(jobDesc.group(1));
        }
        Matcher inline = INLINE_DESCRIPTION_PATTERN.matcher(html);
        String best = null;
        while (inline.find()) {
            String candidate = decodeJsonString(inline.group(1));
            if (candidate.length() > (best != null ? best.length() : 0)) {
                best = candidate;
            }
        }
        return best;
    }

    private String cleanLinkedInOgTitle(String ogTitle) {
        String decoded = decodeHtmlEntities(ogTitle);
        Matcher matcher = LINKEDIN_TITLE_PATTERN.matcher(decoded.trim());
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        int pipe = decoded.indexOf('|');
        if (pipe > 0) {
            return decoded.substring(0, pipe).trim();
        }
        return decoded.trim();
    }

    private boolean isLinkedInLoginWall(String html) {
        String lower = html.toLowerCase(Locale.ROOT);
        return lower.contains("authwall")
                || lower.contains("join linkedin")
                || lower.contains("sign in to view")
                || lower.contains("sign in to see")
                || lower.contains("login-modal");
    }

    private boolean looksLikeLinkedInTitleOnly(String text) {
        if (text == null || text.isBlank()) {
            return true;
        }
        String trimmed = text.trim();
        if (trimmed.length() < MIN_JOB_DESCRIPTION_LENGTH) {
            return LINKEDIN_TITLE_PATTERN.matcher(trimmed).find()
                    || trimmed.toLowerCase(Locale.ROOT).contains("| linkedin");
        }
        return LINKEDIN_TITLE_PATTERN.matcher(trimmed).matches();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String decodeHtmlEntities(String value) {
        if (value == null) {
            return null;
        }
        return HtmlUtils.htmlUnescape(value).trim();
    }

    private Optional<ImportJobResponseDTO> parseLinkedInJsonLd(String html) {
        Matcher scriptMatcher = JSON_LD_SCRIPT_PATTERN.matcher(html);
        while (scriptMatcher.find()) {
            String json = scriptMatcher.group(1);
            if (!json.contains("JobPosting")) {
                continue;
            }
            String title = extractJsonStringField(json, "title");
            String description = extractJsonStringField(json, "description");
            if (description == null || description.isBlank()) {
                continue;
            }

            String cleanDescription = normalizeImportText(decodeJsonString(description));
            ImportJobResponseDTO response = new ImportJobResponseDTO();
            response.setTitle(title != null && !title.isBlank() ? decodeJsonString(title) : extractTitle(cleanDescription));
            JobSections sections = parseJobSections(cleanDescription);
            response.setDescription(sections.description);
            response.setResponsibilities(sections.responsibilities);
            response.setRequirements(sections.requirements);
            response.setSkills(extractSkills(cleanDescription));
            response.setLocation(extractLocation(cleanDescription));
            response.setContractType(guessContractType(cleanDescription));
            response.setExtractedData(new HashMap<>(Map.of("parser", "json-ld")));
            return Optional.of(response);
        }
        return Optional.empty();
    }

    private String extractJsonStringField(String json, String field) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(field) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String decodeJsonString(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\n", "\n")
                .replace("\\r", "")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private String htmlToPlainText(String html) {
        String withoutScripts = SCRIPT_STYLE_PATTERN.matcher(html).replaceAll(" ");
        String text = HTML_TAG_PATTERN.matcher(withoutScripts).replaceAll(" ");
        return cleanPlainText(HtmlUtils.htmlUnescape(text));
    }

    private String cleanPlainText(String text) {
        if (text == null) {
            return "";
        }
        String cleaned = text.replace('\u00A0', ' ');
        for (String marker : PAGE_NOISE_MARKERS) {
            int index = cleaned.indexOf(marker);
            if (index > 0) {
                cleaned = cleaned.substring(0, index);
            }
        }
        cleaned = cleaned
                .replaceAll("(?i)Sign in to access AI-powered advices.*", "")
                .replaceAll("(?i)Use AI to assess how you fit.*", "")
                .replaceAll("(?i)Apply\\s+Join or sign in.*", "")
                .replaceAll("(?is)function getDfd\\(\\).*?window\\.pemTracking = getDfd\\(\\);", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return cleaned;
    }

    private String extractDescriptionBody(String text) {
        String body = text;
        int start = indexOfAny(body.toLowerCase(Locale.ROOT),
                "at the ", "what you'll", "what you’ll", "your mission", "about the role", "job description");
        if (start >= 0) {
            body = body.substring(start);
        }
        body = truncateAtNoise(body);
        if (body.length() > 6000) {
            body = body.substring(0, 6000) + "...";
        }
        if (body.isBlank() || looksLikeLinkedInTitleOnly(body)) {
            return text.length() >= MIN_JOB_DESCRIPTION_LENGTH && !looksLikeLinkedInTitleOnly(text)
                    ? text
                    : "";
        }
        return body;
    }

    private String truncateAtNoise(String text) {
        String result = text;
        for (String marker : PAGE_NOISE_MARKERS) {
            int index = result.indexOf(marker);
            if (index > 80) {
                result = result.substring(0, index);
            }
        }
        return result.trim();
    }

    private String extractTitle(String text) {
        Matcher linkedIn = LINKEDIN_TITLE_PATTERN.matcher(text);
        if (linkedIn.find()) {
            return linkedIn.group(1).trim();
        }

        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.length() <= 120 && !trimmed.toLowerCase(Locale.ROOT).contains("linkedin")) {
                return trimmed;
            }
        }

        if (text.length() <= 120) {
            return text;
        }
        return text.substring(0, Math.min(100, text.length())).trim() + "...";
    }

    private List<String> extractSkills(String text) {
        LinkedHashSet<String> skills = new LinkedHashSet<>();

        Matcher matcher = SKILL_PATTERN.matcher(text);
        while (matcher.find()) {
            Arrays.stream(matcher.group(1).split("[,;|/•]"))
                    .map(String::trim)
                    .filter(skill -> skill.length() > 1 && skill.length() <= 40)
                    .forEach(skills::add);
        }

        if (skills.isEmpty()) {
            String[] commonSkills = {
                    "Java", "Spring Boot", "Angular", "React", "Node.js", "Python", "FastAPI",
                    "SQL", "Docker", "Kubernetes", "AWS", "Git", "TypeScript", "API"
            };
            for (String skill : commonSkills) {
                if (text.toLowerCase(Locale.ROOT).contains(skill.toLowerCase(Locale.ROOT))) {
                    skills.add(skill);
                }
            }
        }

        return skills.stream().limit(12).collect(Collectors.toList());
    }

    private String extractRequirements(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        int requirementsIndex = indexOfAny(lower,
                "you're a great fit", "you are a great fit", "requirements", "qualifications", "profile", "exigences");
        if (requirementsIndex >= 0) {
            return truncateAtNoise(text.substring(requirementsIndex).trim());
        }

        int missionIndex = indexOfAny(lower, "your mission", "what you'll", "what you’ll");
        if (missionIndex >= 0) {
            return truncateAtNoise(text.substring(missionIndex).trim());
        }

        return text.length() > 800 ? text.substring(0, 800) + "..." : "";
    }

    private String normalizeImportText(String text) {
        if (text == null) {
            return "";
        }
        String cleaned = text.trim().replace("\r\n", "\n").replace('\u00A0', ' ');
        for (String marker : PAGE_NOISE_MARKERS) {
            int index = cleaned.indexOf(marker);
            if (index > 0) {
                cleaned = cleaned.substring(0, index);
            }
        }
        cleaned = cleaned
                .replaceAll("(?i)Sign in to access AI-powered advices.*", "")
                .replaceAll("(?i)Use AI to assess how you fit.*", "")
                .replaceAll("(?i)Apply\\s+Join or sign in.*", "")
                .replaceAll("(?is)function getDfd\\(\\).*?window\\.pemTracking = getDfd\\(\\);", " ")
                .replaceAll("[ \\t\\f\\v]+", " ")
                .replaceAll(" *\\n *", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
        return cleaned;
    }

    private static final class JobSections {
        private String description = "";
        private String responsibilities = "";
        private String requirements = "";
    }

    private JobSections parseJobSections(String normalized) {
        JobSections sections = new JobSections();
        if (normalized == null || normalized.isBlank()) {
            return sections;
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        int missionsIdx = indexOfSectionMarker(lower,
                "vos missions", "missions principales", "missions :", "missions:",
                "responsabilités", "responsabilites", "responsabilités :",
                "what you'll", "what you’ll", "your mission", "key responsibilities");
        int profileIdx = indexOfSectionMarker(lower,
                "profil recherché", "profil recherche", "profil requis", "profil :",
                "qualifications", "requirements", "exigences",
                "compétences requises", "competences requises",
                "you're a great fit", "you are a great fit",
                "minimum ", "ans d'expérience", "ans d experience", "bac +");
        int benefitsIdx = indexOfSectionMarker(lower,
                "benefits", "avantages", "ce que nous offrons", "what we offer");

        if (missionsIdx >= 0) {
            sections.description = normalized.substring(0, missionsIdx).trim();
            int respEnd = profileIdx > missionsIdx ? profileIdx : (benefitsIdx > missionsIdx ? benefitsIdx : normalized.length());
            sections.responsibilities = normalized.substring(missionsIdx, respEnd).trim();
        } else {
            sections.description = profileIdx > 0
                    ? normalized.substring(0, profileIdx).trim()
                    : normalized;
        }

        if (profileIdx >= 0) {
            int reqEnd = benefitsIdx > profileIdx ? benefitsIdx : normalized.length();
            sections.requirements = normalized.substring(profileIdx, reqEnd).trim();
            if (missionsIdx < 0 && sections.description.equals(normalized)) {
                sections.description = normalized.substring(0, profileIdx).trim();
            }
        }

        if (missionsIdx < 0 && profileIdx < 0) {
            applyParagraphSplit(normalized, sections);
        }

        sections.responsibilities = stripLeadingSectionHeader(sections.responsibilities);
        sections.requirements = stripLeadingSectionHeader(sections.requirements);
        sections.description = stripLeadingSectionHeader(sections.description);

        if (sections.description.isBlank()) {
            sections.description = firstParagraph(normalized);
        }

        if (sections.responsibilities.isBlank() && sections.requirements.isBlank()
                && isSameText(sections.description, normalized)) {
            splitByFrenchHeuristics(normalized, sections);
        }

        return dedupeSections(sections, normalized);
    }

    private void splitByFrenchHeuristics(String normalized, JobSections sections) {
        String lower = normalized.toLowerCase(Locale.ROOT);
        int actionStart = indexOfSectionMarker(lower,
                "participation", "conception", "développement", "developpement",
                "assurer", "contribuer", "rédaction", "redaction", "mise en place", "veiller");
        int profileStart = indexOfSectionMarker(lower,
                "profil recherché", "profil requis", "minimum ", "ans d'expérience",
                "ans d experience", "bac +", "diplôme", "diplome", "maîtrise", "maitrise");

        if (actionStart > 60) {
            sections.description = normalized.substring(0, actionStart).trim();
            if (profileStart > actionStart) {
                sections.responsibilities = normalized.substring(actionStart, profileStart).trim();
                sections.requirements = normalized.substring(profileStart).trim();
            } else {
                sections.responsibilities = normalized.substring(actionStart).trim();
            }
        } else if (profileStart > 60) {
            sections.description = normalized.substring(0, profileStart).trim();
            sections.requirements = normalized.substring(profileStart).trim();
        }
    }

    private void applyParagraphSplit(String normalized, JobSections sections) {
        String[] paragraphs = normalized.split("\\n\\n+");
        if (paragraphs.length >= 3) {
            sections.description = paragraphs[0].trim();
            sections.responsibilities = paragraphs[1].trim();
            sections.requirements = String.join("\n\n",
                    Arrays.copyOfRange(paragraphs, 2, paragraphs.length)).trim();
        } else if (paragraphs.length == 2) {
            sections.description = paragraphs[0].trim();
            sections.responsibilities = paragraphs[1].trim();
        }
    }

    private int indexOfSectionMarker(String lower, String... markers) {
        int best = -1;
        for (String marker : markers) {
            int idx = lower.indexOf(marker);
            if (idx >= 0 && (best < 0 || idx < best)) {
                best = idx;
            }
        }
        return best;
    }

    private String stripLeadingSectionHeader(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.replaceFirst(
                "(?is)^\\s*(?:job description|description|missions?(?: principales)?|responsabilités?|"
                        + "requirements?|qualifications?|profil recherché|profil requis|exigences|"
                        + "compétences requises|competences requises|benefits?|avantages?)\\s*[:\\-]?\\s*",
                ""
        ).trim();
    }

    private String firstParagraph(String text) {
        String[] parts = text.split("\\n\\n+");
        return parts.length > 0 ? parts[0].trim() : text.trim();
    }

    private JobSections dedupeSections(JobSections sections, String fullText) {
        if (isSameText(sections.description, sections.responsibilities)) {
            sections.responsibilities = "";
        }
        if (isSameText(sections.description, sections.requirements)
                || isSameText(sections.responsibilities, sections.requirements)) {
            sections.requirements = "";
        }

        if (sections.responsibilities.isBlank()) {
            sections.responsibilities = extractInlineSection(fullText, lower ->
                    indexOfSectionMarker(lower, "participation", "conception", "développement", "developpement",
                            "assurer", "contribuer", "mission"));
            if (isSameText(sections.responsibilities, sections.description)) {
                sections.responsibilities = "";
            }
        }

        if (sections.requirements.isBlank()) {
            String req = extractRequirementsSectionOnly(fullText);
            if (!isSameText(req, sections.description) && !isSameText(req, sections.responsibilities)) {
                sections.requirements = req;
            }
        }

        if (sections.description.length() > 6000) {
            sections.description = sections.description.substring(0, 6000) + "...";
        }
        return sections;
    }

    private String extractInlineSection(String text, java.util.function.Function<String, Integer> markerFinder) {
        String lower = text.toLowerCase(Locale.ROOT);
        int start = markerFinder.apply(lower);
        if (start < 0) {
            return "";
        }
        int profileIdx = indexOfSectionMarker(lower,
                "profil recherché", "profil requis", "qualifications", "requirements", "minimum ");
        int end = profileIdx > start ? profileIdx : text.length();
        return text.substring(start, end).trim();
    }

    private String extractRequirementsSectionOnly(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        int requirementsIndex = indexOfSectionMarker(lower,
                "profil recherché", "profil requis", "qualifications", "requirements", "exigences",
                "compétences requises", "competences requises", "you're a great fit", "minimum ", "bac +");
        if (requirementsIndex >= 0) {
            return stripLeadingSectionHeader(text.substring(requirementsIndex).trim());
        }
        return "";
    }

    private boolean isSameText(String a, String b) {
        if (a == null || b == null || a.isBlank() || b.isBlank()) {
            return false;
        }
        String na = a.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        String nb = b.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (na.equals(nb)) {
            return true;
        }
        int compareLen = Math.min(160, Math.min(na.length(), nb.length()));
        return compareLen > 40 && na.regionMatches(0, nb, 0, compareLen);
    }

    private String extractLocation(String text) {
        Matcher matcher = Pattern.compile("(?i)(?:location|lieu|based in|à|in)\\s*[:\\-]?\\s*([A-Za-zÀ-ÿ\\s,.-]{3,60})")
                .matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        Matcher linkedInLocation = Pattern.compile("(?i)in\\s+([A-Za-zÀ-ÿ\\s,.-]{3,40}),\\s*([A-Za-zÀ-ÿ\\s,.-]{2,40})")
                .matcher(text);
        if (linkedInLocation.find()) {
            return linkedInLocation.group(1).trim() + ", " + linkedInLocation.group(2).trim();
        }
        if (text.toLowerCase(Locale.ROOT).contains("tunis")) {
            return "Tunis, Tunisia";
        }
        return null;
    }

    private String guessContractType(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("intern") || lower.contains("stage")) {
            return "STAGE";
        }
        if (lower.contains("apprenti")) {
            return "APPRENTISSAGE";
        }
        if (lower.contains("pfe") || lower.contains("final year")) {
            return "PFE";
        }
        return "EMPLOI";
    }

    private int indexOfAny(String text, String... keywords) {
        int best = -1;
        for (String keyword : keywords) {
            int index = text.indexOf(keyword);
            if (index >= 0 && (best < 0 || index < best)) {
                best = index;
            }
        }
        return best;
    }
}
