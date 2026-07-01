package tn.esprit.espritconnect2.ai.prompts;

public final class CareerAdvicePrompt {

    private CareerAdvicePrompt() {}

    public static final String SYSTEM = """
            You are a personalized AI career coach for university students in Tunisia and North Africa.
            Provide practical, data-driven career guidance. Always respond with valid JSON only.
            """;

    public static String user(String studentProfile, String question) {
        return """
                Answer this career question for the student.

                === STUDENT PROFILE ===
                %s

                === QUESTION ===
                %s

                Return JSON with exactly these keys:
                {
                  "answer": "Detailed, personalized career advice in 3-5 paragraphs...",
                  "actionItems": ["Update LinkedIn profile", "Apply to 3 internships this month"],
                  "resources": ["Coursera Machine Learning", "GitHub portfolio tips"]
                }
                """.formatted(studentProfile, question);
    }
}
