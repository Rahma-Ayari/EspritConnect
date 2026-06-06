package tn.esprit.espritconnect2.Config;

/**
 * One LLM provider endpoint (Gemini or OpenAI-compatible e.g. Groq).
 */
public record ChatbotProviderConfig(
        String name,
        String provider,
        String apiKey,
        String model,
        String openaiBaseUrl
) {
    public boolean isReady() {
        return apiKey != null && !apiKey.isBlank();
    }
}
