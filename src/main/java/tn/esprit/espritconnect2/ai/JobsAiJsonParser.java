package tn.esprit.espritconnect2.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Cleans and parses JSON returned by LLM providers.
 */
public final class JobsAiJsonParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JobsAiJsonParser() {}

    public static JsonNode parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Empty AI response");
        }
        try {
            return MAPPER.readTree(clean(raw));
        } catch (Exception first) {
            try {
                return MAPPER.readTree(repairTruncatedJson(clean(raw)));
            } catch (Exception second) {
                throw new IllegalStateException("Failed to parse AI JSON response: " + first.getMessage(), first);
            }
        }
    }

    public static String clean(String raw) {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("(?s)^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
        }
        return cleaned;
    }

    /**
     * Best-effort repair when the model hits MAX_TOKENS mid-string.
     */
    static String repairTruncatedJson(String json) {
        StringBuilder sb = new StringBuilder(json.trim());

        // Close an open string literal
        int quotes = 0;
        boolean escaped = false;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                quotes++;
            }
        }
        if (quotes % 2 != 0) {
            sb.append('"');
        }

        // Close open brackets/braces
        int braces = 0;
        int brackets = 0;
        boolean inString = false;
        escaped = false;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;
            if (c == '{') braces++;
            else if (c == '}') braces--;
            else if (c == '[') brackets++;
            else if (c == ']') brackets--;
        }
        while (brackets > 0) {
            sb.append(']');
            brackets--;
        }
        while (braces > 0) {
            sb.append('}');
            braces--;
        }
        return sb.toString();
    }
}
