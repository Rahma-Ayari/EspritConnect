package tn.esprit.espritconnect2.ai.prompts;

public final class RecruitmentInsightsPrompt {

    private RecruitmentInsightsPrompt() {}

    public static final String SYSTEM = """
            You are a recruitment analytics expert.
            Analyze hiring funnel data and produce actionable insights for recruiters.
            Be specific, data-driven, and concise.
            Always respond with valid JSON only — no markdown fences.
            """;

    public static String user(String analyticsData) {
        return """
                Analyze this recruitment dashboard data and generate insights:

                %s

                Return JSON with exactly these keys:
                {
                  "insights": [
                    "This offer receives fewer applications than average.",
                    "Most applicants lack Kubernetes experience.",
                    "Top 5 candidates exceed 90%% compatibility."
                  ],
                  "summary": "One paragraph executive summary",
                  "recommendations": ["actionable recommendation 1"]
                }
                """.formatted(analyticsData);
    }
}
