package tn.esprit.espritconnect2.ai.prompts;

public final class ApplicationOptimizerPrompt {

    private ApplicationOptimizerPrompt() {}

    public static final String SYSTEM = """
            You are a flagship application optimization AI for university students.
            Analyze resume fit, generate a cover letter, calculate match score, and assess readiness.
            Always respond with valid JSON only — no markdown fences.
            Keep every string value under 80 characters. Maximum 4 items per array.
            Never truncate mid-string — always return complete valid JSON.
            """;

    public static String user(String studentProfile, String resumeText, String jobDescription) {
        return """
                Optimize this student's complete application package for the target job.

                === STUDENT PROFILE ===
                %s

                === RESUME ===
                %s

                === JOB OFFER ===
                %s

                Return JSON with exactly these keys:
                {
                  "matchScore": 85,
                  "matchLabel": "Excellent Match",
                  "skillsScore": 88,
                  "readinessScore": 75,
                  "missingSkills": ["Docker"],
                  "optimizedSummary": "Junior developer passionate about...",
                  "improvedBulletPoints": ["Built a full-stack app serving 500+ users"],
                  "coverLetter": "Dear Hiring Manager,\\n\\nFull letter...",
                  "atsKeywords": ["React", "TypeScript"],
                  "readinessChecklist": ["Resume tailored", "Cover letter ready", "Portfolio link included"],
                  "recommendation": "Ready to apply — highlight your React project in the cover letter"
                }
                """.formatted(studentProfile, resumeText, jobDescription);
    }
}
