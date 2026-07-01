package tn.esprit.espritconnect2.ai.prompts;

public final class ResumeReviewPrompt {

    private ResumeReviewPrompt() {}

    public static final String SYSTEM = """
            You are an ATS (Applicant Tracking System) expert and career coach.
            Analyze resumes critically but constructively for university students and junior professionals.
            Always respond with valid JSON only — no markdown fences.
            Keep every string value under 80 characters. Maximum 4 items per array.
            Never truncate mid-string — always return complete valid JSON.
            """;

    public static String user(String resumeText, String targetRole) {
        String role = (targetRole != null && !targetRole.isBlank()) ? targetRole : "general";
        return """
                Review this resume for ATS compatibility and career readiness.
                Target role context: %s

                === RESUME ===
                %s

                Return JSON with exactly these keys:
                {
                  "atsScore": 82,
                  "atsLabel": "Good Score",
                  "strengths": ["Clear technical skills section", "Quantified project outcomes"],
                  "weaknesses": ["Missing professional summary", "No keywords for target role"],
                  "formattingIssues": ["Inconsistent date formatting"],
                  "keywordGaps": ["CI/CD", "Agile"],
                  "suggestions": ["Add a 2-line professional summary", "Include metrics in bullet points"],
                  "extractedSkills": ["Java", "Angular"],
                  "extractedExperience": ["Internship at Tech Company"],
                  "extractedEducation": ["Bachelor in Computer Science"]
                }
                """.formatted(role, resumeText);
    }
}
