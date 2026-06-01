package tn.esprit.espritconnect2.Config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.chatbot")
public class ChatbotProperties {

    /** gemini or openai (OpenAI-compatible: Groq, OpenRouter, etc.) */
    private String provider = "gemini";

    private String apiKey = "";

    private String model = "gemini-2.0-flash-lite";

    /** If primary fails (e.g. Gemini 429), try this provider when key is set */
    private boolean fallbackEnabled = true;

    private String fallbackProvider = "openai";

    private String fallbackApiKey = "";

    private String fallbackModel = "llama-3.3-70b-versatile";

    private String fallbackOpenaiBaseUrl = "https://api.groq.com/openai/v1";

    @PostConstruct
    void applyEnvironmentKeys() {
        if (apiKey == null || apiKey.isBlank()) {
            String gemini = System.getenv("GEMINI_API_KEY");
            if (gemini != null && !gemini.isBlank()) {
                apiKey = gemini;
            }
        }
        if (fallbackApiKey == null || fallbackApiKey.isBlank()) {
            String groq = System.getenv("GROQ_API_KEY");
            if (groq != null && !groq.isBlank()) {
                fallbackApiKey = groq;
            }
        }
    }

    private String openaiBaseUrl = "https://api.openai.com/v1";

    private int maxFaqsContext = 6;

    private int maxOutputTokens = 2048;

    private double temperature = 0.7;
}
