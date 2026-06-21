package tn.esprit.espritconnect2.ai.prompts;

public final class GenerateJobDescriptionPrompt {

    private GenerateJobDescriptionPrompt() {}

    public static final String SYSTEM = """
            You are an expert HR copywriter and technical recruiter.
            Generate professional, inclusive, ATS-friendly job offer content.
            Always respond with valid JSON only — no markdown fences, no extra text.
            """;

    public static String user(String title, String skills, String experienceLevel,
                              String contractType, String department, String location,
                              String language, String additionalPrompt) {
        return """
                Generate a complete job offer in %s with these inputs:
                - Job Title: %s
                - Skills: %s
                - Experience Level: %s
                - Contract Type: %s
                - Department: %s
                - Location: %s
                %s

                Return JSON with exactly these keys:
                {
                  "description": "professional overview paragraph",
                  "responsibilities": "bullet list as plain text with • prefix",
                  "requirements": "bullet list as plain text with • prefix",
                  "benefits": "bullet list as plain text with • prefix",
                  "keywords": ["keyword1", "keyword2"],
                  "recruitmentText": "short catchy text for job boards",
                  "suggestedTitle": "%s",
                  "suggestedSkills": ["skill1", "skill2"]
                }
                """.formatted(
                language,
                nullSafe(title),
                nullSafe(skills),
                nullSafe(experienceLevel),
                nullSafe(contractType),
                nullSafe(department),
                nullSafe(location),
                additionalPrompt != null && !additionalPrompt.isBlank()
                        ? "- Additional context: " + additionalPrompt : "",
                nullSafe(title)
        );
    }

    private static String nullSafe(String value) {
        return value == null || value.isBlank() ? "Not specified" : value;
    }
}
