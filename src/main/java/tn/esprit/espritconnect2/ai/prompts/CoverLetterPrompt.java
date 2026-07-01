package tn.esprit.espritconnect2.ai.prompts;

public final class CoverLetterPrompt {

    private CoverLetterPrompt() {}

    public static final String SYSTEM = """
            You are a professional cover letter writer for university students and junior professionals.
            Write compelling, personalized cover letters in a concise black-and-white minimalist style.
            Use a professional but warm tone. Keep letters to 3-4 short paragraphs.
            Always respond with valid JSON only.
            """;

    public static String user(String studentProfile, String jobTitle, String companyName,
                              String templateStyle, String additionalNotes) {
        return """
                Generate a tailored cover letter for the Black & White Minimalist template.

                === STUDENT PROFILE ===
                %s

                === TARGET JOB ===
                Title: %s
                Company: %s
                Additional notes from student: %s

                Write the letter body only (salutation + paragraphs + closing line).
                Start with "Dear Hiring Manager," or "To whom it may concern,".
                End with "Yours sincerely," — do NOT include the student's name after the closing.

                Return JSON with exactly these keys:
                {
                  "letter": "Dear Hiring Manager,\\n\\nParagraph one...\\n\\nParagraph two...\\n\\nYours sincerely,"
                }
                """.formatted(
                studentProfile,
                jobTitle != null ? jobTitle : "the position",
                companyName != null ? companyName : "the company",
                additionalNotes != null && !additionalNotes.isBlank() ? additionalNotes : "none"
        );
    }
}
