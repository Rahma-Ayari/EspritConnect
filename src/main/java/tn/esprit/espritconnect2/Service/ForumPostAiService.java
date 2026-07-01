package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tn.esprit.espritconnect2.DTO.GeneratePostRequest;
import tn.esprit.espritconnect2.DTO.GeneratePostResponse;
import tn.esprit.espritconnect2.Entitie.PostType;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumPostAiService {

    private final ChatbotAiService chatbotAiService;

    public GeneratePostResponse generateContent(GeneratePostRequest request) {
        validateRequest(request);

        String title = request.getTitle().trim();
        PostType type = request.getPostType() != null ? request.getPostType() : PostType.QUESTION;
        List<String> tags = normalizeTags(request.getTags());
        String lang = normalizeLanguage(request.getOutputLanguage());

        String systemPrompt = buildSystemPrompt(lang, type);
        String userPrompt = buildUserPrompt(title, tags);
        String aiContent = chatbotAiService.generateContent(systemPrompt, userPrompt);

        if (StringUtils.hasText(aiContent)) {
            return GeneratePostResponse.builder()
                    .content(aiContent.trim())
                    .aiPowered(true)
                    .disclaimer(en(lang) ? "AI-generated draft — review and edit before publishing." : "Brouillon généré par IA — vérifiez et modifiez avant publication.")
                    .build();
        }

        return GeneratePostResponse.builder()
                .content(buildTemplateContent(title, type, tags, lang))
                .aiPowered(false)
                .disclaimer(en(lang) ? "Template-based draft — configure AI keys for smarter generation." : "Brouillon basé sur un modèle — configurez les clés IA pour une génération intelligente.")
                .build();
    }

    private void validateRequest(GeneratePostRequest request) {
        if (request == null || !StringUtils.hasText(request.getTitle())) {
            throw new IllegalArgumentException("Title is required to generate content.");
        }
        if (request.getTitle().trim().length() < 5) {
            throw new IllegalArgumentException("Title must be at least 5 characters.");
        }
    }

    private String buildSystemPrompt(String lang, PostType type) {
        String typeLabel = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return """
                You are an expert community manager and professional copywriter.
                Your task is to generate high-quality, engaging, and professional forum posts based on the provided title and tags.
                Platform context: ESPRIT Connect, a university career and community platform.
                
                Output requirements:
                - Write entirely in %s.
                - Tone: Professional, clear, engaging, and actionable.
                - Format: Use Markdown formatting (bold, bullet points) to make it highly readable.
                - Length: 150 to 300 words.
                - Post Type: %s. Adapt the structure to fit this specific type of post.
                - Output ONLY the post content. Do not include any greeting, do not repeat the title, and do not add any conversational filler.
                """.formatted(lang, typeLabel);
    }

    private String buildUserPrompt(String title, List<String> tags) {
        String tagLine = tags.isEmpty() ? "none" : String.join(", ", tags);
        return """
                Title: %s
                Tags: %s
                """.formatted(title, tagLine);
    }

    private String buildTemplateContent(String title, PostType type, List<String> tags, String lang) {
        String tagLine = tags.isEmpty() ? "" : "\n\n**Tags:** " + String.join(" ", tags);
        return switch (type) {
            case QUESTION -> en(lang)
                    ? "**Context:** I'm working on \"" + title + "\" and would appreciate guidance from the community.\n\n"
                    + "**What I've tried:** I reviewed course materials and searched existing discussions.\n\n"
                    + "**What I need:** Practical advice, best practices, or examples from your experience."
                    + tagLine
                    : "**Contexte :** Je travaille sur \"" + title + "\" et j'aimerais des conseils de la communauté.\n\n"
                    + "**Ce que j'ai déjà fait :** J'ai consulté les ressources et les discussions existantes.\n\n"
                    + "**Ce dont j'ai besoin :** Des conseils pratiques ou des retours d'expérience."
                    + tagLine;
            case EXPERIENCE -> en(lang)
                    ? "**Experience share:** " + title + "\n\n"
                    + "Here's what worked for me, the challenges I faced, and key lessons learned.\n\n"
                    + "Feel free to ask questions — happy to help others on a similar path."
                    + tagLine
                    : "**Partage d'expérience :** " + title + "\n\n"
                    + "Voici ce qui a fonctionné pour moi, les défis rencontrés et les leçons apprises.\n\n"
                    + "N'hésitez pas à poser des questions."
                    + tagLine;
            case ANNOUNCEMENT -> en(lang)
                    ? "**Announcement:** " + title + "\n\n"
                    + "We are sharing an opportunity with the ESPRIT community. "
                    + "Details below — interested members can reach out through the platform."
                    + tagLine
                    : "**Annonce :** " + title + "\n\n"
                    + "Nous partageons une opportunité avec la communauté ESPRIT. "
                    + "Détails ci-dessous — les membres intéressés peuvent nous contacter via la plateforme."
                    + tagLine;
            case RESOURCE -> en(lang)
                    ? "**Resource:** " + title + "\n\n"
                    + "Useful links, tools, or materials that may help students and alumni.\n\n"
                    + "Add a short summary of why this resource is valuable."
                    + tagLine
                    : "**Ressource :** " + title + "\n\n"
                    + "Liens, outils ou documents utiles pour étudiants et alumni.\n\n"
                    + "Résumé de la valeur de cette ressource."
                    + tagLine;
        };
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .filter(StringUtils::hasText)
                .map(t -> t.startsWith("#") ? t : "#" + t.trim())
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeLanguage(String language) {
        if (language == null) return "French";
        return language.toLowerCase(Locale.ROOT).startsWith("en") ? "English" : "French";
    }

    private boolean en(String lang) {
        return "English".equals(lang);
    }
}
