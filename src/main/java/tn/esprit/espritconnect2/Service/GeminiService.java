package tn.esprit.espritconnect2.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.espritconnect2.DTO.AIAnalysisResponse;
import tn.esprit.espritconnect2.DTO.ActivityLogDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${app.gemini.api-key:YOUR_API_KEY_HERE}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIAnalysisResponse analyzeActivities(List<ActivityLogDto> recentActivities) {
        try {
            // 1. Convert activities to a readable string for the prompt
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("Analyze the following recent user activities and extract a structured list of users.\n");
            promptBuilder.append("For each user found in the logs, determine their last login time, their last profile modification time (if any), and reconstruct a full chronological timeline of their actions (e.g. ['10:00 -> Connected', '10:05 -> Modified profile', '10:10 -> Logged out']).\n");
            promptBuilder.append("Also assess a risk level for each user (Low, Medium, High) based on their activities.\n");
            promptBuilder.append("Provide the response strictly in JSON format matching this exact structure:\n");
            promptBuilder.append("{ \"overallSummary\": \"...\", \"userSummaries\": [ { \"username\": \"...\", \"lastLogin\": \"...\", \"lastProfileUpdate\": \"...\", \"riskLevel\": \"Low|Medium|High\", \"timeline\": [\"...\", \"...\"] } ], \"recommendations\": [\"rec1\"] }\n\n");
            promptBuilder.append("Activity Logs:\n");
            
            for (ActivityLogDto log : recentActivities) {
                promptBuilder.append(String.format("- User: %s, Action: %s, Entity: %s, IP: %s, Time: %s\n",
                        log.getUsername(), log.getAction(), log.getEntity(), log.getIpAddress(), log.getCreatedAt()));
            }

            // 2. Prepare the request body for Gemini API
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> partsMap = new HashMap<>();
            
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", promptBuilder.toString());
            parts.add(part);
            
            partsMap.put("parts", parts);
            contents.add(partsMap);
            requestBody.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // 3. Make the API call
            String url = geminiApiUrl + "?key=" + geminiApiKey;
            String responseStr = restTemplate.postForObject(url, request, String.class);
            
            // 4. Parse the response
            JsonNode rootNode = objectMapper.readTree(responseStr);
            String aiResponseText = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            // Clean markdown JSON formatting if present (e.g. ```json ... ```)
            aiResponseText = aiResponseText.replaceAll("```json", "").replaceAll("```", "").trim();
            
            return objectMapper.readValue(aiResponseText, AIAnalysisResponse.class);

        } catch (Exception e) {
            e.printStackTrace();
            // Return a fallback response in case of errors
            AIAnalysisResponse fallback = new AIAnalysisResponse();
            fallback.setOverallSummary("Failed to analyze activities: " + e.getMessage());
            fallback.setUserSummaries(new ArrayList<>());
            fallback.setRecommendations(List.of("Check Gemini API key configuration"));
            return fallback;
        }
    }
}
