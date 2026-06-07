package tn.esprit.espritconnect2.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tn.esprit.espritconnect2.Config.ChatbotProperties;
import tn.esprit.espritconnect2.Config.ChatbotProviderConfig;
import tn.esprit.espritconnect2.DTO.ChatbotHistoryItemDTO;
import tn.esprit.espritconnect2.Entitie.FAQ;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotAiService {

    private static final String PLATFORM_CONTEXT = """
            You are the ESPRIT Connect AI assistant — a friendly, knowledgeable chatbot for ESPRIT University's \
            career & community platform (not a ticket-only bot).

            About ESPRIT (the university):
            - ESPRIT — École Supérieure Privée d'Ingénierie et de Technologies — is in Tunisia (Greater Tunis area, Ariana / Les Berges du Lac).
            - Main campus area: Pôle Technologique, Chotrana / Berges du Lac, 1082 Tunis, Tunisia.

            About ESPRIT Connect (this platform):
            - Students: profiles, CV upload, job applications, AI job matching, events/workshops, mentoring with alumni, badges, forums
            - Alumni: mentorship and networking
            - Companies: job offers and candidate review
            - All users: messaging, notifications; support tickets exist only for technical/account problems
            - ESPRIT Connect is a web app: users log in via the site (no physical "location" — distinguish university campus vs. the online platform when asked "where").

            What you can help with (use your general knowledge freely):
            - Career advice: CV tips, interviews, internships, skills, choosing a path
            - Platform guidance: how features work, where to find things (offers, events, profile, mentoring)
            - Student life: study tips, productivity, soft skills, networking
            - General conversation: greetings, motivation, explanations of concepts
            - French or English — always match the user's language

            Rules:
            - Think step by step for complex questions; give specific, practical advice — not generic platitudes.
            - Be warm, natural, and conversational like a real chatbot — not a FAQ search engine.
            - Answer general questions fully even when no knowledge-base article is provided.
            - When optional knowledge-base excerpts are provided below, prefer them for exact platform procedures \
            (password reset, submitting tickets, etc.) but still explain clearly in your own words.
            - Ask a short clarifying question when the user's intent is ambiguous (e.g. "ESPRIT campus" vs "ESPRIT Connect app").
            - Do NOT redirect every question to support tickets. Only mention tickets for bugs, login failures, \
            account blocks, or issues you cannot reasonably help with.
            - Do not invent specific URLs, admin emails, or policies; if unsure about a platform detail, say so briefly.
            - Use markdown lightly (**bold**, bullets) when it improves readability.
            """;

    private static final String KB_SECTION_HEADER = """

            --- Optional platform articles (use when relevant; ignore if off-topic) ---
            """;

    private final ChatbotProperties properties;
    private final RestClient chatbotRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isConfigured() {
        return buildProviderChain().stream().anyMatch(ChatbotProviderConfig::isReady);
    }

    public String buildSystemPrompt(String knowledgeContext) {
        if (knowledgeContext == null || knowledgeContext.isBlank()) {
            return PLATFORM_CONTEXT;
        }
        return PLATFORM_CONTEXT + KB_SECTION_HEADER + knowledgeContext;
    }

    public boolean shouldSuggestTicket(String userMessage, String botReply) {
        if (userMessage == null) {
            return false;
        }
        String msg = userMessage.toLowerCase(Locale.ROOT);
        boolean looksLikeTechnicalIssue = msg.contains("bug")
                || msg.contains("error") || msg.contains("erreur")
                || msg.contains("broken") || msg.contains("cassé") || msg.contains("not working")
                || msg.contains("ne marche pas") || msg.contains("ne fonctionne pas")
                || msg.contains("crash") || msg.contains("login") || msg.contains("connexion")
                || msg.contains("password") || msg.contains("mot de passe")
                || msg.contains("account locked") || msg.contains("compte bloqué")
                || msg.contains("hacked") || msg.contains("piraté");

        if (looksLikeTechnicalIssue) {
            return true;
        }
        if (botReply == null) {
            return false;
        }
        String reply = botReply.toLowerCase(Locale.ROOT);
        return reply.contains("support ticket") || reply.contains("ticket de support")
                || reply.contains("soumettre un ticket") || reply.contains("submit a ticket");
    }

    public String generateReply(String userMessage, String knowledgeContext, List<ChatbotHistoryItemDTO> history) {
        if (!isConfigured()) {
            return null;
        }

        String systemPrompt = buildSystemPrompt(knowledgeContext);
        for (ChatbotProviderConfig provider : buildProviderChain()) {
            try {
                String reply = callProvider(provider, userMessage, systemPrompt, history);
                if (reply != null && !reply.isBlank()) {
                    log.debug("Chatbot: answered via {}", provider.name());
                    return reply;
                }
            } catch (RestClientResponseException e) {
                log.warn("Chatbot {} failed ({}): {}", provider.name(), e.getStatusCode(), e.getResponseBodyAsString());
                if ("gemini".equalsIgnoreCase(provider.provider())) {
                    String geminiRetry = tryAlternateGeminiModels(provider, userMessage, systemPrompt, history);
                    if (geminiRetry != null && !geminiRetry.isBlank()) {
                        return geminiRetry;
                    }
                }
            } catch (Exception e) {
                log.warn("Chatbot {} error: {}", provider.name(), e.getMessage());
            }
        }
        return null;
    }

    private List<ChatbotProviderConfig> buildProviderChain() {
        List<ChatbotProviderConfig> chain = new java.util.ArrayList<>();
        chain.add(new ChatbotProviderConfig(
                "primary",
                properties.getProvider(),
                properties.getApiKey(),
                properties.getModel(),
                properties.getOpenaiBaseUrl()
        ));
        if (properties.isFallbackEnabled()) {
            chain.add(new ChatbotProviderConfig(
                    "fallback",
                    properties.getFallbackProvider(),
                    properties.getFallbackApiKey(),
                    properties.getFallbackModel(),
                    properties.getFallbackOpenaiBaseUrl()
            ));
        }
        return chain.stream().filter(ChatbotProviderConfig::isReady).toList();
    }

    private String callProvider(ChatbotProviderConfig provider, String userMessage, String systemPrompt,
                                List<ChatbotHistoryItemDTO> history) {
        String type = provider.provider() == null ? "gemini" : provider.provider().toLowerCase(Locale.ROOT);
        return switch (type) {
            case "openai" -> callOpenAiCompatible(provider, userMessage, systemPrompt, history);
            default -> callGemini(provider, userMessage, systemPrompt, history);
        };
    }

    private String tryAlternateGeminiModels(ChatbotProviderConfig geminiProvider, String userMessage,
                                            String systemPrompt, List<ChatbotHistoryItemDTO> history) {
        for (String model : List.of("gemini-2.0-flash-lite", "gemini-2.0-flash")) {
            if (model.equals(geminiProvider.model())) {
                continue;
            }
            try {
                ChatbotProviderConfig alt = new ChatbotProviderConfig(
                        geminiProvider.name() + "-" + model,
                        "gemini",
                        geminiProvider.apiKey(),
                        model,
                        geminiProvider.openaiBaseUrl()
                );
                String reply = callGemini(alt, userMessage, systemPrompt, history);
                if (reply != null && !reply.isBlank()) {
                    return reply;
                }
            } catch (RestClientResponseException ignored) {
                // try next
            }
        }
        return null;
    }

    public String buildFallbackReply(String userMessage, List<FAQ> matchedFaqs) {
        String lower = userMessage.toLowerCase(Locale.ROOT);

        if (isGreeting(lower)) {
            return "Hello! I'm your ESPRIT Connect assistant. I can chat about careers, CVs, internships, "
                    + "using the platform, events, mentoring, and more. What's on your mind?";
        }

        String topicReply = tryGeneralTopicReply(lower);
        if (topicReply != null) {
            return topicReply;
        }

        // When AI is configured but temporarily unavailable, do not dump raw FAQ text
        if (isConfigured()) {
            return "I'm having a brief connection issue with the AI service. Please try again in a moment. "
                    + "You can also browse **FAQ** in Help & Support, or open a ticket for account problems.";
        }

        if (!matchedFaqs.isEmpty()) {
            FAQ best = matchedFaqs.get(0);
            StringBuilder sb = new StringBuilder();
            sb.append(best.getAnswer());
            if (matchedFaqs.size() > 1) {
                sb.append("\n\nYou might also find these helpful:\n");
                matchedFaqs.stream().skip(1).limit(2).forEach(faq ->
                        sb.append("• ").append(faq.getQuestion()).append("\n"));
            }
            return sb.toString();
        }

        return "I'd love to help with that! Add your **Groq API key** to "
                + "`src/main/resources/application-local.properties` (see application-local.properties.example), "
                + "or set the `GROQ_API_KEY` environment variable, then restart the backend.";
    }

    private String tryGeneralTopicReply(String lower) {
        if (lower.contains("esprit")
                && containsAny(lower, "tell", "more", "about", "learn", "know", "explain", "describe",
                "parle", "dis-moi", "explique", "présente", "presente", "c'est quoi", "what is", "who is", "qui est")) {
            return """
                    **ESPRIT** is a private engineering school in **Tunisia** (Ariana / Les Berges du Lac, Greater Tunis).

                    **ESPRIT Connect** is its online career platform where students, alumni, and companies connect around \
                    jobs, internships, events, mentoring, badges, forums, and support.

                    I can help with careers, using the platform, events, CV tips, and more — what would you like to explore?""";
        }
        if (lower.contains("esprit") && containsAny(lower, "where", "wheres", "where's", "location", "address", "campus", "find", "located",
                "où", "ou ", "adresse", "localisation", "campus", "trouver", "situé", "situe")) {
            return """
                    **ESPRIT University (campus)** is in **Tunisia**, in the **Ariana / Greater Tunis** area — \
                    Pôle Technologique, Chotrana / Les Berges du Lac, 1082 Tunis.

                    **ESPRIT Connect** is the **online career platform** for ESPRIT — you access it by logging into the website/app, \
                    not at a separate physical office. Use it for jobs, events, mentoring, and your profile.

                    Were you looking for the school campus or help using the platform?""";
        }
        if (lower.contains("esprit") && (lower.contains("connect") || lower.contains("what is") || lower.contains("c'est quoi") || lower.contains("qu'est"))) {
            return """
                    **ESPRIT Connect** is ESPRIT University's career and community platform. It helps:

                    • **Students** — build a profile, upload a CV, apply to internships/jobs, get AI job matching, join events, find alumni mentors, earn badges
                    • **Alumni** — offer mentorship and stay connected with the school
                    • **Companies** — publish offers and review candidates

                    You can also message other users, get notifications, and open a support ticket only when something technical goes wrong.

                    What would you like to explore first — jobs, events, or your profile?""";
        }
        if (containsAny(lower, "cv", "resume", "résumé", "curriculum")) {
            return "Here are quick CV tips:\n"
                    + "• Keep it to 1–2 pages, clear sections (Education, Skills, Experience, Projects)\n"
                    + "• Tailor keywords to each job offer\n"
                    + "• Quantify achievements (e.g. \"improved performance by 20%\")\n"
                    + "• Upload your latest version in **Profile → Documents** on ESPRIT Connect.";
        }
        if (containsAny(lower, "interview", "entretien", "embauche")) {
            return "Interview prep basics:\n"
                    + "• Research the company and role\n"
                    + "• Prepare STAR examples (Situation, Task, Action, Result)\n"
                    + "• Practice aloud and prepare 2–3 questions for them\n"
                    + "• Check matching offers on ESPRIT Connect and apply with an updated profile.";
        }
        if (containsAny(lower, "internship", "stage", "job", "emploi", "offer", "offre", "career", "carrière")) {
            return "On ESPRIT Connect you can browse **job offers**, see **AI matching** scores with your skills, "
                    + "and apply directly. Complete your profile and competences first — stronger profiles get better matches!";
        }
        if (containsAny(lower, "mentor", "mentoring", "alumni")) {
            return "ESPRIT Connect connects students with **alumni mentors**. Look for mentors in your field, "
                    + "check availability, and send a mentoring request from the Mentoring section.";
        }
        if (containsAny(lower, "event", "workshop", "atelier", "événement")) {
            return "Browse **Events** on the platform to find workshops and career sessions. Register in one click "
                    + "and you'll get a confirmation notification.";
        }
        if (containsAny(lower, "skill", "competence", "compétence", "learn", "apprendre")) {
            return "Identify skills demanded in offers you like, then close gaps with projects, certifications, "
                    + "and workshops. Add competences to your ESPRIT Connect profile so matching works better.";
        }
        return null;
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGreeting(String lower) {
        return lower.matches("^(hi|hello|hey|bonjour|salut|bjr|coucou|bonsoir)[!.?\\s]*$")
                || lower.contains("good morning") || lower.contains("good evening");
    }

    private String callGemini(ChatbotProviderConfig provider, String userMessage, String systemPrompt,
                              List<ChatbotHistoryItemDTO> history) {
        return callGeminiWithModel(provider, userMessage, systemPrompt, history, provider.model());
    }

    private String callGeminiWithModel(ChatbotProviderConfig provider, String userMessage, String systemPrompt,
                                       List<ChatbotHistoryItemDTO> history, String model) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + provider.apiKey();

        ObjectNode body = objectMapper.createObjectNode();
        body.putObject("systemInstruction")
                .putArray("parts")
                .addObject()
                .put("text", systemPrompt);

        ArrayNode contents = body.putArray("contents");
        appendGeminiHistory(contents, history);
        contents.addObject()
                .put("role", "user")
                .putArray("parts")
                .addObject()
                .put("text", userMessage);

        body.putObject("generationConfig")
                .put("temperature", properties.getTemperature())
                .put("maxOutputTokens", properties.getMaxOutputTokens());

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                JsonNode response = chatbotRestClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);

                String text = extractGeminiText(response);
                if (text != null && !text.isBlank()) {
                    return text;
                }
                log.warn("Chatbot: empty response from model {} (attempt {})", model, attempt);
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                log.error("Chatbot API error model={} status={} body={}", model, status, e.getResponseBodyAsString());
                if (status == 429 && attempt < maxAttempts) {
                    sleepBeforeRetry(attempt);
                    continue;
                }
                throw e;
            }
        }
        return null;
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(1500L * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void appendGeminiHistory(ArrayNode contents, List<ChatbotHistoryItemDTO> history) {
        if (history == null) {
            return;
        }
        for (ChatbotHistoryItemDTO item : history) {
            if (item.getContent() == null || item.getContent().isBlank()) {
                continue;
            }
            String role = "assistant".equalsIgnoreCase(item.getRole()) ? "model" : "user";
            contents.addObject()
                    .put("role", role)
                    .putArray("parts")
                    .addObject()
                    .put("text", item.getContent());
        }
    }

    private String extractGeminiText(JsonNode response) {
        if (response == null) {
            return null;
        }
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

    private String callOpenAiCompatible(ChatbotProviderConfig provider, String userMessage, String systemPrompt,
                                        List<ChatbotHistoryItemDTO> history) {
        String baseUrl = provider.openaiBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.openai.com/v1";
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String url = baseUrl + "/chat/completions";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", provider.model());
        body.put("temperature", properties.getTemperature());
        body.put("max_tokens", properties.getMaxOutputTokens());

        ArrayNode messages = body.putArray("messages");
        messages.addObject()
                .put("role", "system")
                .put("content", systemPrompt);

        if (history != null) {
            for (ChatbotHistoryItemDTO item : history) {
                if (item.getContent() == null || item.getContent().isBlank()) {
                    continue;
                }
                String role = "assistant".equalsIgnoreCase(item.getRole()) ? "assistant" : "user";
                messages.addObject().put("role", role).put("content", item.getContent());
            }
        }

        messages.addObject().put("role", "user").put("content", userMessage);

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                JsonNode response = chatbotRestClient.post()
                        .uri(url)
                        .header("Authorization", "Bearer " + provider.apiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);

                if (response == null) {
                    continue;
                }
                String text = response.path("choices").path(0).path("message").path("content").asText(null);
                if (text != null && !text.isBlank()) {
                    return text;
                }
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                log.error("Chatbot Groq/OpenAI error model={} status={} body={}",
                        provider.model(), status, e.getResponseBodyAsString());
                if (status == 429 && attempt < maxAttempts) {
                    sleepBeforeRetry(attempt);
                    continue;
                }
                throw e;
            }
        }
        return null;
    }
}
