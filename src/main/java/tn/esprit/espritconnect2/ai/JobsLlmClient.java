package tn.esprit.espritconnect2.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobsLlmClient {

    private static final int MAX_TOKENS_CAP = 8192;
    private static final String CONCISE_SUFFIX = """

            IMPORTANT: Return complete valid JSON only.
            - Max 4 items per array.
            - Each string value max 80 characters.
            - Do not truncate mid-string.
            """;

    private final JobsAiProperties properties;
    private final RestClient jobsAiRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record LlmResult(String text, String providerLabel) {}

    public LlmResult complete(String systemPrompt, String userPrompt) {
        return complete(systemPrompt, userPrompt, properties.getMaxOutputTokens());
    }

    public LlmResult complete(String systemPrompt, String userPrompt, int maxOutputTokens) {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("No AI provider configured. Set GEMINI_API_KEY or OPENAI_API_KEY.");
        }

        String primary = properties.getProvider() == null ? "gemini" : properties.getProvider().toLowerCase(Locale.ROOT);
        try {
            return callProvider(primary, systemPrompt, userPrompt, maxOutputTokens);
        } catch (Exception e) {
            log.warn("Jobs AI primary provider {} failed: {}", primary, e.getMessage());
            if (!properties.isFallbackEnabled()) {
                throw e instanceof RuntimeException re ? re : new RuntimeException(e);
            }
            String fallback = "gemini".equals(primary) ? "openai" : "gemini";
            if (isProviderReady(fallback)) {
                return callProvider(fallback, systemPrompt, userPrompt, maxOutputTokens);
            }
            throw e instanceof RuntimeException re ? re : new RuntimeException(e);
        }
    }

    private boolean isProviderReady(String providerType) {
        if ("openai".equalsIgnoreCase(providerType)) {
            return properties.isOpenAiReady();
        }
        return properties.isGeminiReady();
    }

    private LlmResult callProvider(String providerType, String systemPrompt, String userPrompt, int maxOutputTokens) {
        if ("openai".equalsIgnoreCase(providerType)) {
            if (!properties.isOpenAiReady()) {
                throw new IllegalStateException("OpenAI API key is not configured");
            }
            String text = callOpenAi(systemPrompt, userPrompt, maxOutputTokens);
            return new LlmResult(text, "OpenAI");
        }
        if (!properties.isGeminiReady()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }
        String text = callGemini(systemPrompt, userPrompt, maxOutputTokens);
        return new LlmResult(text, "Gemini AI");
    }

    private String callGemini(String systemPrompt, String userPrompt, int maxOutputTokens) {
        GeminiCallResult first = callGeminiOnce(systemPrompt, userPrompt, maxOutputTokens);
        if (first.truncated() && maxOutputTokens < MAX_TOKENS_CAP) {
            log.warn("Gemini response truncated at {} tokens, retrying with {}", maxOutputTokens, MAX_TOKENS_CAP);
            GeminiCallResult retry = callGeminiOnce(systemPrompt, userPrompt + CONCISE_SUFFIX, MAX_TOKENS_CAP);
            if (retry.text() != null && !retry.text().isBlank()) {
                return retry.text();
            }
        }
        if (first.truncated()) {
            log.warn("Gemini response still truncated at max tokens — JSON repair may be needed");
        }
        return first.text();
    }

    private GeminiCallResult callGeminiOnce(String systemPrompt, String userPrompt, int maxOutputTokens) {
        String model = properties.getGeminiModel();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + properties.getGeminiApiKey();

        ObjectNode body = objectMapper.createObjectNode();
        body.putObject("systemInstruction")
                .putArray("parts")
                .addObject()
                .put("text", systemPrompt);
        body.putArray("contents")
                .addObject()
                .put("role", "user")
                .putArray("parts")
                .addObject()
                .put("text", userPrompt);
        body.putObject("generationConfig")
                .put("temperature", properties.getTemperature())
                .put("maxOutputTokens", maxOutputTokens)
                .put("responseMimeType", "application/json");

        JsonNode response = postWithRetry(url, body, null);
        JsonNode candidates = response.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return new GeminiCallResult(null, false);
        }
        JsonNode candidate = candidates.get(0);
        String finishReason = candidate.path("finishReason").asText("");
        boolean truncated = "MAX_TOKENS".equals(finishReason);
        String text = extractGeminiTextFromCandidate(candidate);
        return new GeminiCallResult(text, truncated);
    }

    private String callOpenAi(String systemPrompt, String userPrompt, int maxOutputTokens) {
        String baseUrl = properties.getOpenaiBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.openai.com/v1";
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String url = baseUrl + "/chat/completions";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getOpenaiModel());
        body.put("temperature", properties.getTemperature());
        body.put("max_tokens", maxOutputTokens);
        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", systemPrompt);
        messages.addObject().put("role", "user").put("content", userPrompt);
        body.putObject("response_format").put("type", "json_object");

        JsonNode response = postWithRetry(url, body, properties.getOpenaiApiKey());
        JsonNode choice = response.path("choices").path(0);
        String finishReason = choice.path("finish_reason").asText("");
        String text = choice.path("message").path("content").asText(null);
        if ("length".equals(finishReason) && maxOutputTokens < MAX_TOKENS_CAP) {
            log.warn("OpenAI response truncated, retrying with concise prompt");
            return callOpenAi(systemPrompt, userPrompt + CONCISE_SUFFIX, MAX_TOKENS_CAP);
        }
        return text;
    }

    private JsonNode postWithRetry(String url, ObjectNode body, String bearerToken) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                RestClient.RequestBodySpec spec = jobsAiRestClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON);
                if (bearerToken != null) {
                    spec = spec.header("Authorization", "Bearer " + bearerToken);
                }
                JsonNode response = spec.body(body).retrieve().body(JsonNode.class);
                if (response != null) {
                    return response;
                }
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                if ((status == 429 || status == 503) && attempt < maxAttempts) {
                    log.warn("AI provider returned {}, retrying ({}/{})", status, attempt, maxAttempts);
                    sleep(1500L * attempt);
                    continue;
                }
                if (status == 503) {
                    throw new IllegalStateException(
                            "The AI service is temporarily busy. Please try again in a moment.");
                }
                if (status == 429) {
                    throw new IllegalStateException(
                            "Too many AI requests right now. Please try again in a minute.");
                }
                throw e;
            }
        }
        throw new IllegalStateException("Empty response from AI provider");
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String extractGeminiTextFromCandidate(JsonNode candidate) {
        JsonNode parts = candidate.path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return null;
        }
        return parts.get(0).path("text").asText(null);
    }

    private record GeminiCallResult(String text, boolean truncated) {}
}
