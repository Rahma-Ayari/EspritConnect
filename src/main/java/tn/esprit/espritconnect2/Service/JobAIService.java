package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tn.esprit.espritconnect2.DTO.AIJobGenerateRequestDTO;
import tn.esprit.espritconnect2.DTO.AIJobGenerateResponseDTO;
import tn.esprit.espritconnect2.DTO.AIImproveTextRequestDTO;
import tn.esprit.espritconnect2.ai.JobsRecruitmentAiService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobAIService {

    private final JobsRecruitmentAiService llmService;

    public AIJobGenerateResponseDTO generateJobDescription(AIJobGenerateRequestDTO request) {
        validateGenerateRequest(request);
        if (llmService.isConfigured()) {
            return llmService.generateJobDescription(request);
        }
        return generateRuleBased(request);
    }

    public AIJobGenerateResponseDTO improveJobDescription(AIImproveTextRequestDTO request) {
        if (request == null || !StringUtils.hasText(request.getOriginalText())) {
            throw new IllegalArgumentException("Original text is required");
        }
        if (llmService.isConfigured()) {
            return llmService.improveJobDescription(request);
        }
        return improveRuleBased(request);
    }

    private AIJobGenerateResponseDTO generateRuleBased(AIJobGenerateRequestDTO request) {
        String lang = normalizeLanguage(request.getOutputLanguage());
        String title = resolveTitle(request);
        String department = resolveDepartment(request);
        String location = resolveLocation(request);
        List<String> skills = request.getSkills() != null ? request.getSkills() : List.of();
        String experience = request.getExperienceLevel() != null ? request.getExperienceLevel() : "INTERMEDIATE";
        String contractType = request.getContractType() != null ? request.getContractType() : "EMPLOI";
        String prompt = request.getAdditionalPrompt() != null ? request.getAdditionalPrompt().trim() : "";

        AIJobGenerateResponseDTO response = new AIJobGenerateResponseDTO();
        response.setSuggestedTitle(title);
        response.setSuggestedSkills(skills.isEmpty() ? inferSkillsFromPrompt(prompt, title) : skills);
        response.setDescription(buildDescription(lang, title, department, location, skills, prompt));
        response.setResponsibilities(buildResponsibilities(lang, title, experience, skills, prompt));
        response.setRequirements(buildRequirements(lang, experience, skills, prompt));
        response.setBenefits(buildBenefits(lang, contractType));
        response.setKeywords(buildKeywords(title, department, experience, skills));
        response.setAiDisclaimer(localizedDisclaimer(lang) + " (Rule-based fallback — configure GEMINI_API_KEY for real AI.)");
        return response;
    }

    private AIJobGenerateResponseDTO improveRuleBased(AIImproveTextRequestDTO request) {
        String lang = normalizeLanguage(request.getOutputLanguage());
        String title = StringUtils.hasText(request.getJobTitle()) ? request.getJobTitle().trim() : "Job Offer";
        String improved = request.getOriginalText().trim();
        if (!improved.endsWith(".")) {
            improved += ".";
        }

        AIJobGenerateResponseDTO response = new AIJobGenerateResponseDTO();
        response.setSuggestedTitle(title);
        response.setDescription(improved + "\n\n" + localizedImprovementNote(lang, title));
        response.setResponsibilities(buildResponsibilities(lang, title, "INTERMEDIATE", List.of(), request.getOriginalText()));
        response.setRequirements(buildRequirements(lang, "INTERMEDIATE", List.of(), request.getOriginalText()));
        response.setBenefits(buildBenefits(lang, "EMPLOI"));
        response.setKeywords(List.of(title, "professional", "teamwork"));
        response.setAiDisclaimer(localizedDisclaimer(lang) + " (Rule-based fallback — configure GEMINI_API_KEY for real AI.)");
        return response;
    }

    private void validateGenerateRequest(AIJobGenerateRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        boolean hasPrompt = StringUtils.hasText(request.getAdditionalPrompt());
        boolean hasStructuredData = StringUtils.hasText(request.getTitle()) && StringUtils.hasText(request.getDepartment());

        if (!hasPrompt && !hasStructuredData) {
            throw new IllegalArgumentException("Provide a prompt or fill in at least title and department");
        }
    }

    private String normalizeLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return "en";
        }
        String normalized = language.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("fr")) {
            return "fr";
        }
        if (normalized.startsWith("ar")) {
            return "ar";
        }
        return "en";
    }

    private String resolveTitle(AIJobGenerateRequestDTO request) {
        if (StringUtils.hasText(request.getTitle())) {
            return request.getTitle().trim();
        }
        return inferTitleFromPrompt(request.getAdditionalPrompt());
    }

    private String resolveDepartment(AIJobGenerateRequestDTO request) {
        if (StringUtils.hasText(request.getDepartment())) {
            return request.getDepartment().trim();
        }
        return switch (normalizeLanguage(request.getOutputLanguage())) {
            case "fr" -> "Technologie";
            case "ar" -> "التكنولوجيا";
            default -> "Technology";
        };
    }

    private String resolveLocation(AIJobGenerateRequestDTO request) {
        if (StringUtils.hasText(request.getLocation())) {
            return request.getLocation().trim();
        }
        return "Tunis";
    }

    private String inferTitleFromPrompt(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            return "Job Opening";
        }

        String firstLine = Arrays.stream(prompt.split("\n"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(prompt.trim());

        if (firstLine.length() > 80) {
            firstLine = firstLine.substring(0, 77) + "...";
        }
        return firstLine;
    }

    private List<String> inferSkillsFromPrompt(String prompt, String title) {
        Set<String> skills = new LinkedHashSet<>();
        String source = ((prompt != null ? prompt : "") + " " + (title != null ? title : "")).toLowerCase(Locale.ROOT);

        List<String> catalog = List.of(
                "Java", "Spring Boot", "Angular", "React", "Node.js", "Python", "SQL",
                "Docker", "Kubernetes", "AWS", "Git", "TypeScript", "Communication", "Leadership"
        );

        for (String skill : catalog) {
            if (source.contains(skill.toLowerCase(Locale.ROOT))) {
                skills.add(skill);
            }
        }

        if (skills.isEmpty()) {
            skills.addAll(List.of("Communication", "Problem Solving", "Teamwork"));
        }

        return new ArrayList<>(skills);
    }

    private String buildDescription(String lang, String title, String department, String location,
                                    List<String> skills, String prompt) {
        String skillsText = skills.isEmpty() ? "" : String.join(", ", skills);
        String promptBlock = StringUtils.hasText(prompt) ? "\n\n" + prompt : "";

        return switch (lang) {
            case "fr" -> "Nous recherchons un(e) " + title + " pour rejoindre notre équipe "
                    + department + " à " + location + ". "
                    + (skillsText.isEmpty() ? "" : "Vous travaillerez avec " + skillsText + ". ")
                    + "Ce poste est une opportunité de contribuer à des projets innovants dans un environnement dynamique."
                    + promptBlock;
            case "ar" -> "نبحث عن " + title + " للانضمام إلى فريق " + department + " في " + location + ". "
                    + (skillsText.isEmpty() ? "" : "ستعمل باستخدام " + skillsText + ". ")
                    + "هذا الدور يمثل فرصة للمساهمة في مشاريع مبتكرة ضمن بيئة عمل محفزة."
                    + promptBlock;
            default -> "We are looking for a " + title + " to join our " + department
                    + " team in " + location + ". "
                    + (skillsText.isEmpty() ? "" : "You will work with " + skillsText + ". ")
                    + "This role offers the opportunity to contribute to innovative projects in a dynamic environment."
                    + promptBlock;
        };
    }

    private String buildResponsibilities(String lang, String title, String experience,
                                           List<String> skills, String prompt) {
        List<String> lines = switch (lang) {
            case "fr" -> List.of(
                    "• Développer et livrer des fonctionnalités de qualité",
                    "• Collaborer avec les équipes produit et technique",
                    "• Participer aux revues de code et à l'amélioration continue",
                    "• Documenter les livrables et les décisions techniques"
            );
            case "ar" -> List.of(
                    "• تطوير وتسليم ميزات عالية الجودة",
                    "• التعاون مع فرق المنتج والتقنية",
                    "• المشاركة في مراجعات الشفرة والتحسين المستمر",
                    "• توثيق المخرجات والقرارات التقنية"
            );
            default -> List.of(
                    "• Develop and deliver high-quality features",
                    "• Collaborate with product and engineering teams",
                    "• Participate in code reviews and continuous improvement",
                    "• Document deliverables and technical decisions"
            );
        };

        List<String> result = new ArrayList<>(lines);
        if ("SENIOR".equals(experience) || "EXPERT".equals(experience)) {
            result.add(switch (lang) {
                case "fr" -> "• Encadrer les profils juniors et guider les choix techniques";
                case "ar" -> "• إرشاد المطورين junior ودعم القرارات التقنية";
                default -> "• Mentor junior team members and guide technical decisions";
            });
        }
        if (StringUtils.hasText(prompt)) {
            result.add("• " + summarizePrompt(prompt, lang));
        }
        if (!skills.isEmpty()) {
            result.add(switch (lang) {
                case "fr" -> "• Utiliser " + String.join(", ", skills) + " au quotidien";
                case "ar" -> "• استخدام " + String.join(", ", skills) + " بشكل يومي";
                default -> "• Use " + String.join(", ", skills) + " in day-to-day work";
            });
        }
        return String.join("\n", result);
    }

    private String buildRequirements(String lang, String experience, List<String> skills, String prompt) {
        List<String> lines = new ArrayList<>();
        lines.add(switch (lang) {
            case "fr" -> "Exigences:";
            case "ar" -> "المتطلبات:";
            default -> "Requirements:";
        });
        lines.add(switch (lang) {
            case "fr" -> "• Diplôme en informatique ou domaine équivalent";
            case "ar" -> "• شهادة في علوم الحاسوب أو مجال مماثل";
            default -> "• Degree in computer science or related field";
        });
        lines.add(switch (experience) {
            case "JUNIOR" -> switch (lang) {
                case "fr" -> "• 0 à 2 ans d'expérience";
                case "ar" -> "• من 0 إلى 2 سنة خبرة";
                default -> "• 0 to 2 years of experience";
            };
            case "SENIOR" -> switch (lang) {
                case "fr" -> "• 5+ ans d'expérience";
                case "ar" -> "• أكثر من 5 سنوات خبرة";
                default -> "• 5+ years of experience";
            };
            case "EXPERT" -> switch (lang) {
                case "fr" -> "• 10+ ans d'expérience et leadership technique";
                case "ar" -> "• أكثر من 10 سنوات خبرة مع قيادة تقنية";
                default -> "• 10+ years of experience with technical leadership";
            };
            default -> switch (lang) {
                case "fr" -> "• 2 à 5 ans d'expérience";
                case "ar" -> "• من 2 إلى 5 سنوات خبرة";
                default -> "• 2 to 5 years of experience";
            };
        });

        if (!skills.isEmpty()) {
            lines.add(switch (lang) {
                case "fr" -> "• Maîtrise de: " + String.join(", ", skills);
                case "ar" -> "• إتقان: " + String.join(", ", skills);
                default -> "• Proficiency in: " + String.join(", ", skills);
            });
        }

        lines.add(switch (lang) {
            case "fr" -> "• Excellentes compétences en communication et résolution de problèmes";
            case "ar" -> "• مهارات ممتازة في التواصل وحل المشكلات";
            default -> "• Strong communication and problem-solving skills";
        });

        if (StringUtils.hasText(prompt)) {
            lines.add("• " + summarizePrompt(prompt, lang));
        }

        return String.join("\n", lines);
    }

    private String buildBenefits(String lang, String contractType) {
        List<String> lines = switch (lang) {
            case "fr" -> new ArrayList<>(List.of(
                    "Ce que nous offrons:",
                    "• Environnement de travail moderne",
                    "• Projets stimulants et formation continue",
                    "• Équipe collaborative",
                    "• Flexibilité horaire et télétravail possible"
            ));
            case "ar" -> new ArrayList<>(List.of(
                    "ما نقدمه:",
                    "• بيئة عمل حديثة",
                    "• مشاريع محفزة وتدريب مستمر",
                    "• فريق تعاوني",
                    "• مرونة في الوقت وإمكانية العمل عن بعد"
            ));
            default -> new ArrayList<>(List.of(
                    "What we offer:",
                    "• Modern work environment",
                    "• Challenging projects and continuous learning",
                    "• Collaborative team culture",
                    "• Flexible hours and remote-friendly setup"
            ));
        };

        if ("EMPLOI".equals(contractType) || "FULL_TIME".equals(contractType)) {
            lines.add(switch (lang) {
                case "fr" -> "• Package salarial compétitif";
                case "ar" -> "• حزمة أجر تنافسية";
                default -> "• Competitive compensation package";
            });
        }

        return String.join("\n", lines);
    }

    private List<String> buildKeywords(String title, String department, String experience, List<String> skills) {
        List<String> keywords = new ArrayList<>();
        if (StringUtils.hasText(title)) keywords.add(title);
        if (StringUtils.hasText(department)) keywords.add(department);
        if (StringUtils.hasText(experience)) keywords.add(experience);
        keywords.addAll(skills.stream().limit(5).collect(Collectors.toList()));
        return keywords;
    }

    private String summarizePrompt(String prompt, String lang) {
        String trimmed = prompt.trim();
        if (trimmed.length() <= 120) {
            return trimmed;
        }
        String excerpt = trimmed.substring(0, 117) + "...";
        return switch (lang) {
            case "fr" -> "Contexte demandé: " + excerpt;
            case "ar" -> "السياق المطلوب: " + excerpt;
            default -> "Requested context: " + excerpt;
        };
    }

    private String localizedDisclaimer(String lang) {
        return switch (lang) {
            case "fr" -> "Ce contenu a été généré par l'assistant IA. Veuillez le relire avant publication.";
            case "ar" -> "تم إنشاء هذا المحتوى بواسطة المساعد الذكي. يرجى مراجعته قبل النشر.";
            default -> "This content was generated by the AI assistant. Please review it before publishing.";
        };
    }

    private String localizedImprovementNote(String lang, String title) {
        return switch (lang) {
            case "fr" -> "Ce poste de " + title + " offre une excellente opportunité de progression professionnelle.";
            case "ar" -> "يوفر هذا الدور (" + title + ") فرصة ممتازة للتطور المهني.";
            default -> "This " + title + " role offers a strong opportunity for professional growth.";
        };
    }
}
