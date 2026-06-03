package tn.esprit.espritconnect2.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tn.esprit.espritconnect2.DTO.ImportJobResponseDTO;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class JobImportService {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern SKILL_PATTERN = Pattern.compile(
            "(?i)(?:skills?|technologies?|competences?|requirements?)\\s*[:\\-]\\s*(.+)"
    );

    private final RestClient restClient = RestClient.create();

    public ImportJobResponseDTO importFromText(String text, String source) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Job text is required for import");
        }

        String normalized = text.trim().replace("\r\n", "\n");
        ImportJobResponseDTO response = new ImportJobResponseDTO();
        response.setTitle(extractTitle(normalized));
        response.setDescription(normalized);
        response.setSkills(extractSkills(normalized));
        response.setRequirements(extractRequirements(normalized));
        response.setLocation(extractLocation(normalized));
        response.setContractType(guessContractType(normalized));
        response.setExtractedData(Map.of(
                "source", source != null ? source : "TEXT",
                "lineCount", normalized.split("\n").length
        ));
        return response;
    }

    public ImportJobResponseDTO importFromUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL is required for import");
        }

        try {
            String html = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            if (html == null || html.isBlank()) {
                throw new IllegalArgumentException("Could not fetch content from URL");
            }

            String text = HTML_TAG_PATTERN.matcher(html).replaceAll(" ")
                    .replaceAll("\\s+", " ")
                    .trim();
            return importFromText(text, "URL");
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to import from URL: " + e.getMessage());
        }
    }

    private String extractTitle(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.length() <= 120) {
                return trimmed;
            }
        }
        return "Imported Job Offer";
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
                    "Java", "Spring Boot", "Angular", "React", "Node.js", "Python",
                    "SQL", "Docker", "Kubernetes", "AWS", "Git", "TypeScript"
            };
            for (String skill : commonSkills) {
                if (text.toLowerCase().contains(skill.toLowerCase())) {
                    skills.add(skill);
                }
            }
        }

        return skills.stream().limit(12).collect(Collectors.toList());
    }

    private String extractRequirements(String text) {
        String lower = text.toLowerCase();
        int requirementsIndex = indexOfAny(lower, "requirements", "qualifications", "profile", "exigences");
        if (requirementsIndex >= 0) {
            return text.substring(requirementsIndex).trim();
        }

        String[] lines = text.split("\n");
        List<String> bulletLines = Arrays.stream(lines)
                .map(String::trim)
                .filter(line -> line.startsWith("•") || line.startsWith("-") || line.startsWith("*"))
                .limit(8)
                .collect(Collectors.toList());

        if (!bulletLines.isEmpty()) {
            return String.join("\n", bulletLines);
        }

        return text.length() > 500 ? text.substring(0, 500) + "..." : text;
    }

    private String extractLocation(String text) {
        Matcher matcher = Pattern.compile("(?i)(?:location|lieu|based in|à)\\s*[:\\-]?\\s*([A-Za-zÀ-ÿ\\s,-]{3,40})")
                .matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        if (text.toLowerCase().contains("tunis")) {
            return "Tunis";
        }
        return null;
    }

    private String guessContractType(String text) {
        String lower = text.toLowerCase();
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
