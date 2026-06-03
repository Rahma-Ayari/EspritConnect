package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.OffreAiSuggestionRequestDTO;
import tn.esprit.espritconnect2.DTO.OffreAiSuggestionResponseDTO;
import tn.esprit.espritconnect2.DTO.OffreRequestDTO;
import tn.esprit.espritconnect2.DTO.OffreResponseDTO;
import tn.esprit.espritconnect2.Entitie.Type;
import tn.esprit.espritconnect2.Service.IOffreService;
import tn.esprit.espritconnect2.Service.OffreAiService;

import java.util.List;

// DISABLED: This old controller conflicts with the new JobOfferController
// The new JobOfferController provides comprehensive JOBS module functionality
// @RestController
// @RequestMapping("/api/offres")
@RequiredArgsConstructor
public class OffreController {

    private final IOffreService offreService;
    private final OffreAiService offreAiService;

    @PostMapping
    public ResponseEntity<OffreResponseDTO> create(@Valid @RequestBody OffreRequestDTO dto) {
        return new ResponseEntity<>(offreService.createOffre(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OffreResponseDTO>> getAll() {
        return ResponseEntity.ok(offreService.getAllOffres());
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<List<OffreResponseDTO>> getByEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(offreService.getOffresByEntreprise(entrepriseId));
    }

    @GetMapping("/public/search")
    public ResponseEntity<List<OffreResponseDTO>> searchPublic(
            @RequestParam(required = false) String domaine,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) Type typeOffre) {
        return ResponseEntity.ok(offreService.searchPublic(domaine, localisation, typeOffre));
    }

    @PostMapping("/ai/suggest")
    public ResponseEntity<OffreAiSuggestionResponseDTO> aiSuggest(@RequestBody OffreAiSuggestionRequestDTO dto) {
        return ResponseEntity.ok(offreAiService.suggest(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OffreResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.getOffreById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OffreResponseDTO> update(@PathVariable Long id, @Valid @RequestBody OffreRequestDTO dto) {
        return ResponseEntity.ok(offreService.updateOffre(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offreService.deleteOffre(id);
        return ResponseEntity.noContent().build();
    }
}
