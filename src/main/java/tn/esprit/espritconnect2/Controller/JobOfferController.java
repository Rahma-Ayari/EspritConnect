package tn.esprit.espritconnect2.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.*;
import tn.esprit.espritconnect2.Service.JobAIService;
import tn.esprit.espritconnect2.Service.JobImportService;
import tn.esprit.espritconnect2.Service.JobOfferService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offres")
@CrossOrigin(origins = "*")
public class JobOfferController {

    @Autowired
    private JobOfferService jobOfferService;
    
    @Autowired
    private JobAIService jobAIService;

    @Autowired
    private JobImportService jobImportService;

    /**
     * Get all jobs with filtering
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) List<String> contractType,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "recent") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        
        Map<String, Object> result = jobOfferService.getJobs(
            search, status, contractType, department, location, 
            sortBy, sortOrder, page, limit
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Get archived jobs
     */
    @GetMapping("/archived")
    public ResponseEntity<Map<String, Object>> getArchivedJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "recent") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        Map<String, Object> result = jobOfferService.getArchivedJobs(
            search, sortBy, sortOrder, page, limit
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Get job by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobOfferDTO> getJobById(@PathVariable Long id) {
        try {
            JobOfferDTO job = jobOfferService.getJobById(id);
            return ResponseEntity.ok(job);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get jobs by enterprise
     */
    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<List<JobOfferDTO>> getJobsByEnterprise(@PathVariable Long entrepriseId) {
        List<JobOfferDTO> jobs = jobOfferService.getJobsByEnterprise(entrepriseId);
        return ResponseEntity.ok(jobs);
    }

    /**
     * Create new job
     */
    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody JobOfferDTO jobDTO) {
        try {
            JobOfferDTO created = jobOfferService.createJob(jobDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Erreur lors de la création de l'offre: " + e.getMessage()));
        }
    }

    /**
     * Update job
     */
    @PutMapping("/{id}")
    public ResponseEntity<JobOfferDTO> updateJob(
            @PathVariable Long id,
            @RequestBody JobOfferDTO jobDTO) {
        try {
            JobOfferDTO updated = jobOfferService.updateJob(id, jobDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete job
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        try {
            jobOfferService.deleteJob(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Duplicate job
     */
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<JobOfferDTO> duplicateJob(@PathVariable Long id) {
        try {
            JobOfferDTO duplicated = jobOfferService.duplicateJob(id);
            return ResponseEntity.ok(duplicated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Archive job
     */
    @PatchMapping("/{id}/archive")
    public ResponseEntity<?> archiveJob(@PathVariable Long id) {
        try {
            JobOfferDTO archived = jobOfferService.archiveJob(id);
            return ResponseEntity.ok(archived);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Archive failed: " + e.getMessage()));
        }
    }

    /**
     * Restore archived job
     */
    @PatchMapping("/{id}/restore")
    public ResponseEntity<JobOfferDTO> restoreJob(@PathVariable Long id) {
        try {
            JobOfferDTO restored = jobOfferService.restoreJob(id);
            return ResponseEntity.ok(restored);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Pause applications
     */
    @PatchMapping("/{id}/pause")
    public ResponseEntity<JobOfferDTO> pauseApplications(@PathVariable Long id) {
        try {
            JobOfferDTO paused = jobOfferService.pauseApplications(id);
            return ResponseEntity.ok(paused);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Resume applications
     */
    @PatchMapping("/{id}/resume")
    public ResponseEntity<JobOfferDTO> resumeApplications(@PathVariable Long id) {
        try {
            JobOfferDTO resumed = jobOfferService.resumeApplications(id);
            return ResponseEntity.ok(resumed);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Close job
     */
    @PatchMapping("/{id}/close")
    public ResponseEntity<JobOfferDTO> closeJob(@PathVariable Long id) {
        try {
            JobOfferDTO closed = jobOfferService.closeJob(id);
            return ResponseEntity.ok(closed);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Pin job
     */
    @PatchMapping("/{id}/pin")
    public ResponseEntity<JobOfferDTO> pinJob(@PathVariable Long id) {
        try {
            JobOfferDTO pinned = jobOfferService.pinJob(id);
            return ResponseEntity.ok(pinned);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Unpin job
     */
    @PatchMapping("/{id}/unpin")
    public ResponseEntity<JobOfferDTO> unpinJob(@PathVariable Long id) {
        try {
            JobOfferDTO unpinned = jobOfferService.unpinJob(id);
            return ResponseEntity.ok(unpinned);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Save draft
     */
    @PostMapping("/draft")
    public ResponseEntity<JobOfferDTO> saveDraft(@RequestBody JobOfferDTO jobDTO) {
        try {
            JobOfferDTO draft = jobOfferService.saveDraft(jobDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(draft);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update draft
     */
    @PutMapping("/draft/{id}")
    public ResponseEntity<JobOfferDTO> updateDraft(
            @PathVariable Long id,
            @RequestBody JobOfferDTO jobDTO) {
        try {
            JobOfferDTO updated = jobOfferService.updateDraft(id, jobDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== AI Endpoints ==========

    /**
     * Generate job description with AI
     */
    @PostMapping("/ai/generate")
    public ResponseEntity<?> generateJobDescription(
            @RequestBody AIJobGenerateRequestDTO request) {
        try {
            AIJobGenerateResponseDTO response = jobAIService.generateJobDescription(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "AI generation failed: " + e.getMessage()));
        }
    }

    /**
     * Improve existing job description with AI
     */
    @PostMapping("/ai/improve")
    public ResponseEntity<?> improveJobDescription(
            @RequestBody AIImproveTextRequestDTO request) {
        try {
            AIJobGenerateResponseDTO response = jobAIService.improveJobDescription(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "AI improvement failed: " + e.getMessage()));
        }
    }

    /**
     * Import job offer from URL, text, or PDF file
     */
    @PostMapping("/import")
    public ResponseEntity<?> importJob(
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(defaultValue = "TEXT") String source) {
        try {
            ImportJobResponseDTO response;
            if ("URL".equalsIgnoreCase(source)) {
                response = jobImportService.importFromUrl(url);
            } else if ("PDF".equalsIgnoreCase(source)) {
                if (file == null || file.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "message", "PDF file is required. You can also paste the job text using TEXT import."
                    ));
                }
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "PDF parsing is not supported yet. Please paste the job text using TEXT import."
                ));
            } else {
                response = jobImportService.importFromText(text, source);
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Import failed: " + e.getMessage()));
        }
    }
}
