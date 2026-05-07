package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.MatchingComputeRequestDTO;
import tn.esprit.espritconnect2.DTO.MatchingResponseDTO;
import tn.esprit.espritconnect2.Service.IMatchingService;

import java.util.List;

@RestController
@RequestMapping("/api/matchings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MatchingController {

    private final IMatchingService matchingService;

    @PostMapping("/compute")
    public ResponseEntity<MatchingResponseDTO> compute(@Valid @RequestBody MatchingComputeRequestDTO dto) {
        return ResponseEntity.ok(matchingService.computeMatching(dto.getEtudiantId(), dto.getOffreId()));
    }

    @PostMapping("/recompute/student/{studentId}")
    public ResponseEntity<List<MatchingResponseDTO>> recomputeForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(matchingService.recomputeForEtudiant(studentId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<MatchingResponseDTO>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(matchingService.getByEtudiant(studentId));
    }

    @GetMapping("/offre/{offreId}/top")
    public ResponseEntity<List<MatchingResponseDTO>> getTopByOffre(
            @PathVariable Long offreId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(matchingService.getTopByOffre(offreId, limit));
    }
}
