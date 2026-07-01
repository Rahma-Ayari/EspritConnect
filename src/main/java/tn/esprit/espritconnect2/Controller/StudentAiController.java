package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.studentai.*;
import tn.esprit.espritconnect2.ai.ResumeTextExtractor;
import tn.esprit.espritconnect2.ai.StudentAiService;

import java.util.Map;

@RestController
@RequestMapping("/api/student-ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentAiController {

    private final StudentAiService studentAiService;
    private final ResumeTextExtractor resumeTextExtractor;

    @PostMapping(value = "/extract-resume", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> extractResume(@RequestParam("file") MultipartFile file) {
        currentEmail();
        String text = resumeTextExtractor.extract(file);
        return ResponseEntity.ok(Map.of("text", text));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "configured", studentAiService.isConfigured(),
                "message", studentAiService.isConfigured()
                        ? "Student AI career center is ready"
                        : "Set GEMINI_API_KEY or OPENAI_API_KEY to enable AI features"
        ));
    }

    @PostMapping("/job-match")
    public ResponseEntity<StudentJobMatchResponseDTO> jobMatch(@Valid @RequestBody StudentJobMatchRequestDTO request) {
        return ResponseEntity.ok(studentAiService.jobMatch(currentEmail(), request));
    }

    @PostMapping("/review-resume")
    public ResponseEntity<StudentResumeReviewResponseDTO> reviewResume(@Valid @RequestBody StudentResumeReviewRequestDTO request) {
        return ResponseEntity.ok(studentAiService.reviewResume(currentEmail(), request));
    }

    @PostMapping("/optimize-resume")
    public ResponseEntity<StudentResumeOptimizerResponseDTO> optimizeResume(@Valid @RequestBody StudentResumeOptimizerRequestDTO request) {
        return ResponseEntity.ok(studentAiService.optimizeResume(currentEmail(), request));
    }

    @PostMapping("/generate-cover-letter")
    public ResponseEntity<StudentCoverLetterResponseDTO> generateCoverLetter(@Valid @RequestBody StudentCoverLetterRequestDTO request) {
        return ResponseEntity.ok(studentAiService.generateCoverLetter(currentEmail(), request));
    }

    @PostMapping("/interview-preparation")
    public ResponseEntity<StudentInterviewPrepResponseDTO> interviewPreparation(@Valid @RequestBody StudentInterviewPrepRequestDTO request) {
        return ResponseEntity.ok(studentAiService.interviewPreparation(currentEmail(), request));
    }

    @PostMapping("/application-optimizer")
    public ResponseEntity<StudentApplicationOptimizerResponseDTO> applicationOptimizer(@Valid @RequestBody StudentApplicationOptimizerRequestDTO request) {
        return ResponseEntity.ok(studentAiService.applicationOptimizer(currentEmail(), request));
    }

    @PostMapping("/career-advice")
    public ResponseEntity<StudentCareerAdviceResponseDTO> careerAdvice(@Valid @RequestBody StudentCareerAdviceRequestDTO request) {
        return ResponseEntity.ok(studentAiService.careerAdvice(currentEmail(), request));
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required for student AI features");
        }
        return auth.getName();
    }
}
