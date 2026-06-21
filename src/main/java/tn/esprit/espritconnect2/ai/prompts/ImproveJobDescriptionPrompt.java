package tn.esprit.espritconnect2.ai.prompts;

public final class ImproveJobDescriptionPrompt {

    private ImproveJobDescriptionPrompt() {}

    public static final String SYSTEM = """
            You are an expert recruiter and copy editor.
            Improve job offers: fix grammar, improve formatting, add missing sections,
            make content more attractive and ATS-compatible.
            Always respond with valid JSON only — no markdown fences.
            """;

    public static String user(String originalText, String jobTitle, String language) {
        return """
                Improve this job offer (%s). Job title: %s

                Original content:
                %s

                Return JSON with exactly these keys:
                {
                  "description": "improved professional description",
                  "responsibilities": "improved responsibilities with • bullets",
                  "requirements": "improved requirements with • bullets",
                  "benefits": "improved benefits with • bullets",
                  "keywords": ["ats", "keywords"],
                  "recruitmentText": "short catchy recruitment text",
                  "suggestedTitle": "%s",
                  "suggestedSkills": ["inferred", "skills"]
                }
                """.formatted(
                language,
                nullSafe(jobTitle),
                originalText,
                nullSafe(jobTitle)
        );
    }

    private static String nullSafe(String value) {
        return value == null || value.isBlank() ? "Job Offer" : value;
    }
}
