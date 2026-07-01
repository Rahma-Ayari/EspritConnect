package tn.esprit.espritconnect2.ai.prompts;

public final class InterviewPrepPrompt {

    private InterviewPrepPrompt() {}

    public static final String SYSTEM = """
            You are an interview preparation coach for technical and HR interviews.
            Generate realistic questions with suggested answers tailored to the role.
            Always respond with valid JSON only — no markdown fences.
            """;

    public static String user(String jobTitle, String jobDescription, String studentProfile) {
        return """
                Prepare interview questions and tips for this student applying to this role.

                === STUDENT PROFILE ===
                %s

                === JOB ===
                Title: %s
                Description: %s

                Return JSON with exactly these keys:
                {
                  "technicalQuestions": ["Explain REST vs GraphQL"],
                  "behavioralQuestions": ["Tell me about a time you handled conflict"],
                  "hrQuestions": ["Why do you want to join our company?"],
                  "suggestedAnswers": ["For REST vs GraphQL: REST is resource-based..."],
                  "interviewTips": ["Research the company culture", "Prepare 2 questions for the interviewer"]
                }
                """.formatted(studentProfile, jobTitle, jobDescription);
    }
}
