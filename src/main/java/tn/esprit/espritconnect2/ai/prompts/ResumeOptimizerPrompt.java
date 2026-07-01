package tn.esprit.espritconnect2.ai.prompts;

public final class ResumeOptimizerPrompt {

    private ResumeOptimizerPrompt() {}

    public static final String SYSTEM = """
            You are an ATS optimization expert. Tailor resumes to specific job descriptions.
            Provide actionable, copy-ready improvements. Always respond with valid JSON only.
            Keep every string value under 80 characters. Maximum 4 items per array.
            Never truncate mid-string — always return complete valid JSON.
            """;

    public static String user(String resumeText, String jobDescription) {
        return """
                Optimize this resume for the target job.

                === RESUME ===
                %s

                === TARGET JOB ===
                %s

                Return JSON with exactly these keys:
                {
                  "atsScore": 78,
                  "atsLabel": "Good Score",
                  "optimizedSummary": "Results-driven junior developer with...",
                  "improvedBulletPoints": ["Led a team of 4 to deliver...", "Reduced API response time by 30%..."],
                  "atsKeywords": ["React", "TypeScript", "REST API", "Agile"],
                  "missingSkills": ["Docker", "AWS"],
                  "suggestions": ["Add cloud deployment experience", "Quantify project impact"]
                }
                """.formatted(resumeText, jobDescription);
    }
}
