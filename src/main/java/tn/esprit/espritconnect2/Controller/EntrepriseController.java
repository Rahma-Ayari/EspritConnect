package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.EntrepriseRequestDTO;
import tn.esprit.espritconnect2.DTO.EntrepriseResponseDTO;
import tn.esprit.espritconnect2.Service.IEntrepriseService;

import java.util.List;

@RestController
@RequestMapping("/api/entreprises")
@RequiredArgsConstructor
public class EntrepriseController {

    private final IEntrepriseService entrepriseService;

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
}
