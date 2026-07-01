package tn.esprit.espritconnect2.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(JobsAiProperties.class)
@Slf4j
public class JobsAiConfig {

    @Bean
    public RestClient jobsAiRestClient() {
        return RestClient.builder().build();
    }

    @Bean
    ApplicationRunner jobsAiStartupCheck(JobsAiProperties properties) {
        return args -> {
            if (!properties.isConfigured()) {
                log.warn("Jobs AI: no API key. Set GEMINI_API_KEY or OPENAI_API_KEY (see application.properties)");
            } else {
                log.info("Jobs AI enabled — primary={}/{} fallback={}",
                        properties.getProvider(),
                        "gemini".equalsIgnoreCase(properties.getProvider())
                                ? properties.getGeminiModel() : properties.getOpenaiModel(),
                        properties.isFallbackEnabled() ? properties.getFallbackProvider() : "off");
            }
        };
    }
}
