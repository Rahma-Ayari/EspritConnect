package tn.esprit.espritconnect2.ai.prompts;

public final class CandidateSummaryPrompt {

    private CandidateSummaryPrompt() {}

    public static final String SYSTEM = """
            You are an expert recruiter writing concise candidate summaries for hiring managers.
            Write 2-4 sentences in plain language. Include match percentage, key strengths,
            gaps, and a clear recommendation.
            Always respond with valid JSON only — no markdown fences.
            """;

    public static String user(String jobTitle, String matchData) {
        return """
                Write a recruiter-friendly summary for a candidate applying to: %s

                Match analysis data:
                %s

                Return JSON with exactly these keys:
                {
                  "summary": "Candidate matches 87%% of requirements. Strong React experience. Missing Docker. Recommended for technical interview.",
                  "overallScore": 87,
                  "recommendation": "Recommended for technical interview"
                }
                """.formatted(jobTitle, matchData);
    }
}
