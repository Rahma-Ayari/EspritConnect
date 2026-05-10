package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.CandidatureRequestDTO;
import tn.esprit.espritconnect2.DTO.CandidatureResponseDTO;
import tn.esprit.espritconnect2.DTO.CandidatureStatusUpdateDTO;
import tn.esprit.espritconnect2.Service.ICandidatureService;

import java.util.List;

@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CandidatureController {

    private final ICandidatureService candidatureService;

    @PostMapping
    public ResponseEntity<CandidatureResponseDTO> create(@Valid @RequestBody CandidatureRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(candidatureService.create(dto));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CandidatureResponseDTO>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(candidatureService.getByEtudiant(studentId));
    }

    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<CandidatureResponseDTO>> getByOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(candidatureService.getByOffre(offreId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CandidatureResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CandidatureStatusUpdateDTO dto) {
        return ResponseEntity.ok(candidatureService.updateStatus(id, dto.getStatut()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        candidatureService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
