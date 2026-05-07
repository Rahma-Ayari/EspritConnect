package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.OffreRequestDTO;
import tn.esprit.espritconnect2.DTO.OffreResponseDTO;
import tn.esprit.espritconnect2.Service.IOffreService;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
public class OffreController {

    private final IOffreService offreService;

    @PostMapping
    public ResponseEntity<OffreResponseDTO> create(@Valid @RequestBody OffreRequestDTO dto) {
        return new ResponseEntity<>(offreService.createOffre(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OffreResponseDTO>> getAll() {
        return ResponseEntity.ok(offreService.getAllOffres());
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
