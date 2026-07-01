package tn.esprit.espritconnect2.ai.prompts;

public final class JobImportExtractPrompt {

    private JobImportExtractPrompt() {}

    public static final String SYSTEM = """
            You are an expert at parsing job postings from URLs, PDFs, or raw text.
            Extract structured job offer fields accurately.
            Always respond with valid JSON only — no markdown fences.
            """;

    public static String user(String rawContent, String source) {
        String truncated = rawContent.length() > 12000
                ? rawContent.substring(0, 12000) + "\n...[truncated]"
                : rawContent;
        return """
                Extract job offer fields from this %s content:

                %s

                Return JSON with exactly these keys:
                {
                  "title": "job title",
                  "contractType": "STAGE|EMPLOI|APPRENTISSAGE|PFE or best guess",
                  "experienceLevel": "JUNIOR|INTERMEDIATE|SENIOR|EXPERT",
                  "location": "city, country",
                  "skills": ["skill1", "skill2"],
                  "responsibilities": "bullet list text",
                  "requirements": "bullet list text",
                  "benefits": "bullet list text",
                  "description": "full description paragraph"
                }
                """.formatted(source, truncated);
    }
}
