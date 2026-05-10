package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.CompetenceRequestDTO;
import tn.esprit.espritconnect2.DTO.CompetenceResponseDTO;
import tn.esprit.espritconnect2.Service.CompetenceServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/competences")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompetenceController {

    private final CompetenceServiceImpl competenceService;

    @PostMapping
    public ResponseEntity<CompetenceResponseDTO> ajouter(@Valid @RequestBody CompetenceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(competenceService.ajouterCompetence(dto));
    }

    @GetMapping
    public ResponseEntity<List<CompetenceResponseDTO>> getAll() {
        return ResponseEntity.ok(competenceService.getAllCompetences());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetenceResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(competenceService.getCompetenceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetenceResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CompetenceRequestDTO dto) {
        return ResponseEntity.ok(competenceService.updateCompetence(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        competenceService.deleteCompetence(id);
        return ResponseEntity.noContent().build();
    }
}
