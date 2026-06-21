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

    private final JobsAiProperties properties;
    private final RestClient jobsAiRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record LlmResult(String text, String providerLabel) {}

    public LlmResult complete(String systemPrompt, String userPrompt) {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("No AI provider configured. Set GEMINI_API_KEY or OPENAI_API_KEY.");
        }

        String primary = properties.getProvider() == null ? "gemini" : properties.getProvider().toLowerCase(Locale.ROOT);
        try {
            return callProvider(primary, systemPrompt, userPrompt);
        } catch (Exception e) {
            log.warn("Jobs AI primary provider {} failed: {}", primary, e.getMessage());
            if (!properties.isFallbackEnabled()) {
                throw e instanceof RuntimeException re ? re : new RuntimeException(e);
            }
            String fallback = "gemini".equals(primary) ? "openai" : "gemini";
            return callProvider(fallback, systemPrompt, userPrompt);
        }
    }

    private LlmResult callProvider(String providerType, String systemPrompt, String userPrompt) {
        if ("openai".equalsIgnoreCase(providerType)) {
            if (!properties.isOpenAiReady()) {
                throw new IllegalStateException("OpenAI API key is not configured");
            }
            String text = callOpenAi(systemPrompt, userPrompt);
            return new LlmResult(text, "OpenAI");
        }
        if (!properties.isGeminiReady()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }
        String text = callGemini(systemPrompt, userPrompt);
        return new LlmResult(text, "Gemini AI");
    }

    private String callGemini(String systemPrompt, String userPrompt) {
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
                .put("maxOutputTokens", properties.getMaxOutputTokens())
                .put("responseMimeType", "application/json");

        JsonNode response = postWithRetry(url, body, null);
        return extractGeminiText(response);
    }

    private String callOpenAi(String systemPrompt, String userPrompt) {
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
        body.put("max_tokens", properties.getMaxOutputTokens());
        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", systemPrompt);
        messages.addObject().put("role", "user").put("content", userPrompt);
        body.putObject("response_format").put("type", "json_object");

        JsonNode response = postWithRetry(url, body, properties.getOpenaiApiKey());
        return response.path("choices").path(0).path("message").path("content").asText(null);
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
                if (e.getStatusCode().value() == 429 && attempt < maxAttempts) {
                    sleep(1500L * attempt);
                    continue;
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

    private String extractGeminiText(JsonNode response) {
        JsonNode candidates = response.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return null;
        }
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return null;
        }
        return parts.get(0).path("text").asText(null);
    }
}
