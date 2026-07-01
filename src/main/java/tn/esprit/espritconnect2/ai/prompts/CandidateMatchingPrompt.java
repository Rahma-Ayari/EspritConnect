package tn.esprit.espritconnect2.ai.prompts;

public final class CandidateMatchingPrompt {

    private CandidateMatchingPrompt() {}

    public static final String SYSTEM = """
            You are an expert technical recruiter.
            Compare candidate profiles against job offers objectively.
            Score each dimension 0-100. Be specific about skills and experience gaps.
            Always respond with valid JSON only — no markdown fences.
            """;

    public static String user(String jobProfile, String candidateProfile) {
        return """
                Compare this candidate against the job offer.

                === JOB OFFER ===
                %s

                === CANDIDATE PROFILE ===
                %s

                Return JSON with exactly these keys:
                {
                  "overallScore": 87,
                  "skillsScore": 90,
                  "experienceScore": 80,
                  "educationScore": 85,
                  "matchingSkills": ["React", "TypeScript"],
                  "missingSkills": ["Docker"],
                  "strengths": ["strength1"],
                  "weaknesses": ["weakness1"],
                  "recommendation": "Recommended for technical interview"
                }
                """.formatted(jobProfile, candidateProfile);
    }
}
