package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.*;
import tn.esprit.espritconnect2.Service.EntrepriseJobDashboardService;
import tn.esprit.espritconnect2.Service.EntrepriseVerificationService;
import tn.esprit.espritconnect2.Service.IEntrepriseService;

import java.util.List;

@RestController
@RequestMapping("/api/entreprises")
@RequiredArgsConstructor
public class EntrepriseController {

    private final IEntrepriseService entrepriseService;
    private final EntrepriseVerificationService verificationService;
    private final EntrepriseJobDashboardService jobDashboardService;

    @PostMapping
    public ResponseEntity<EntrepriseResponseDTO> create(@Valid @RequestBody EntrepriseRequestDTO dto) {
        return new ResponseEntity<>(entrepriseService.createEntreprise(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EntrepriseResponseDTO>> getAll() {
        return ResponseEntity.ok(entrepriseService.getAllEntreprises());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntrepriseResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(entrepriseService.getEntrepriseById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntrepriseResponseDTO> update(@PathVariable Long id, @Valid @RequestBody EntrepriseRequestDTO dto) {
        return ResponseEntity.ok(entrepriseService.updateEntreprise(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        entrepriseService.deleteEntreprise(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/job-dashboard")
    public ResponseEntity<EntrepriseJobDashboardDTO> jobDashboard(@PathVariable Long id) {
        return ResponseEntity.ok(jobDashboardService.getOverview(id));
    }

    @GetMapping("/{id}/verification")
    public ResponseEntity<EntrepriseVerificationDTO> verificationStatus(@PathVariable Long id) {
        return ResponseEntity.ok(verificationService.getStatus(id));
    }

    @PostMapping("/{id}/verification/documents")
    public ResponseEntity<EntrepriseDocumentDTO> uploadDocument(
            @PathVariable Long id,
            @Valid @RequestBody EntrepriseDocumentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(verificationService.addDocument(id, dto));
    }

    @PostMapping("/{id}/verification/submit")
    public ResponseEntity<EntrepriseVerificationDTO> submitVerification(@PathVariable Long id) {
        return ResponseEntity.ok(verificationService.submitForReview(id));
    }
}
