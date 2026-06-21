package tn.esprit.espritconnect2.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class JobsDashboardAiController {

    private final JobsRecruitmentAiService aiService;

    @PostMapping("/generate-job")
    public ResponseEntity<?> generateJob(@RequestBody AIJobGenerateRequestDTO request) {
        return execute(() -> aiService.generateJob(request));
    }

    @PostMapping("/improve-job")
    public ResponseEntity<?> improveJob(@RequestBody AIImproveTextRequestDTO request) {
        return execute(() -> aiService.improveJob(request));
    }

    @PostMapping("/import-extract")
    public ResponseEntity<?> importExtract(@RequestBody AiImportExtractRequestDTO request) {
        return execute(() -> aiService.extractImport(request));
    }

    @PostMapping("/match-candidate")
    public ResponseEntity<?> matchCandidate(@RequestBody AiMatchCandidateRequestDTO request) {
        return execute(() -> aiService.matchCandidate(request));
    }

    @PostMapping("/candidate-summary")
    public ResponseEntity<?> candidateSummary(@RequestBody AiCandidateSummaryRequestDTO request) {
        return execute(() -> aiService.candidateSummary(request));
    }

    @PostMapping("/recruitment-insights")
    public ResponseEntity<?> recruitmentInsights(@RequestBody AiRecruitmentInsightsRequestDTO request) {
        return execute(() -> aiService.recruitmentInsights(request));
    }

    private ResponseEntity<?> execute(SupplierWithException<?> supplier) {
        try {
            if (!aiService.isConfigured()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("message", "AI service not configured. Set GEMINI_API_KEY or OPENAI_API_KEY on the server."));
            }
            return ResponseEntity.ok(supplier.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Jobs AI error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "AI request failed: " + e.getMessage()));
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
