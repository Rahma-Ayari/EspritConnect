package tn.esprit.espritconnect2.ai.prompts;

public final class JobMatchingPrompt {

    private JobMatchingPrompt() {}

    public static final String SYSTEM = """
            You are an expert career coach and technical recruiter for university students.
            Compare student profiles against job offers objectively and constructively.
            Score each dimension 0-100. Be specific about skills, projects, and experience gaps.
            Always respond with valid JSON only — no markdown fences.
            """;

    public static String user(String studentProfile, String jobDescription, String resumeText) {
        String resumeSection = (resumeText != null && !resumeText.isBlank())
                ? "\n=== RESUME / CV ===\n" + resumeText.trim() + "\n"
                : "";
        return """
                Analyze how well this student matches the job opportunity.

                === STUDENT PROFILE ===
                %s
                %s
                === JOB OFFER ===
                %s

                Return JSON with exactly these keys:
                {
                  "overallScore": 87,
                  "skillsScore": 90,
                  "projectsScore": 75,
                  "experienceScore": 80,
                  "educationScore": 85,
                  "matchingSkills": ["React", "TypeScript"],
                  "missingSkills": ["Docker", "Kubernetes"],
                  "improvementSuggestions": ["Add a cloud deployment project", "Highlight team leadership"],
                  "recommendation": "Strong match — apply with a tailored cover letter highlighting your React projects"
                }
                """.formatted(studentProfile, resumeSection, jobDescription);
    }
}
