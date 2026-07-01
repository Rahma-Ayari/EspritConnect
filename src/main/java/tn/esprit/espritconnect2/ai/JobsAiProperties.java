package tn.esprit.espritconnect2.ai;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jobs-ai")
public class JobsAiProperties {

    /** gemini or openai */
    private String provider = "gemini";

    private String geminiApiKey = "";

    private String openaiApiKey = "";

    private String geminiModel = "gemini-2.5-flash";

    private String openaiModel = "gpt-4o-mini";

    private String openaiBaseUrl = "https://api.openai.com/v1";

    private boolean fallbackEnabled = true;

    private String fallbackProvider = "openai";

    private int maxOutputTokens = 4096;

    private double temperature = 0.35;

    private long cacheTtlMinutes = 30;

    @PostConstruct
    void applyEnvironmentKeys() {
        String geminiEnv = System.getenv("GEMINI_API_KEY");
        String openaiEnv = System.getenv("OPENAI_API_KEY");

        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            if (geminiEnv != null && !geminiEnv.isBlank()) {
                geminiApiKey = geminiEnv;
            }
        }
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            if (openaiEnv != null && !openaiEnv.isBlank()) {
                openaiApiKey = openaiEnv;
            }
        }

        if ((geminiApiKey == null || geminiApiKey.isBlank())
                && (openaiApiKey == null || openaiApiKey.isBlank())) {
            String chatbotGemini = System.getenv("VITE_GEMINI_API_KEY");
            String chatbotOpenai = System.getenv("VITE_OPENAI_API_KEY");
            if (chatbotGemini != null && !chatbotGemini.isBlank()) {
                geminiApiKey = chatbotGemini;
            }
            if (chatbotOpenai != null && !chatbotOpenai.isBlank()) {
                openaiApiKey = chatbotOpenai;
            }
        }
    }

    public boolean isGeminiReady() {
        return geminiApiKey != null && !geminiApiKey.isBlank();
    }

    public boolean isOpenAiReady() {
        return openaiApiKey != null && !openaiApiKey.isBlank();
    }

    public boolean isConfigured() {
        return isGeminiReady() || isOpenAiReady();
    }
}
