package tn.esprit.espritconnect2.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ChatbotProperties.class)
@Slf4j
public class ChatbotConfig {

    @Bean
    public RestClient chatbotRestClient() {
        return RestClient.builder().build();
    }

    @Bean
    ApplicationRunner chatbotStartupCheck(ChatbotProperties properties) {
        return args -> {
            boolean primary = properties.getApiKey() != null && !properties.getApiKey().isBlank();
            boolean fallback = properties.isFallbackEnabled()
                    && properties.getFallbackApiKey() != null && !properties.getFallbackApiKey().isBlank();
            if (!primary && !fallback) {
                log.warn("Chatbot: no API key. Add Groq (recommended) or Gemini key — see application-local.properties.example");
            } else {
                log.info("Chatbot: AI enabled primary={}/{} fallback={}/{}",
                        properties.getProvider(), properties.getModel(),
                        fallback ? properties.getFallbackProvider() : "off",
                        fallback ? properties.getFallbackModel() : "-");
            }
        };
    }
}
