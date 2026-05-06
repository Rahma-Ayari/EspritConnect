package tn.esprit.espritconnect2.Controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.EtudiantRequestDTO;
import tn.esprit.espritconnect2.DTO.EtudiantResponseDTO;
import tn.esprit.espritconnect2.Service.EtudiantServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")

public class EtudiantController {

    private final EtudiantServiceImpl etudiantService;

    // POST /api/etudiants
    @PostMapping
    public ResponseEntity<EtudiantResponseDTO> ajouter(
            @Valid @RequestBody EtudiantRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(etudiantService.ajouterEtudiant(dto));
    }

    // GET /api/etudiants
    @GetMapping
    public ResponseEntity<List<EtudiantResponseDTO>> getAll() {
        return ResponseEntity.ok(etudiantService.getAllEtudiants());
    }

    // GET /api/etudiants/{id}
    @GetMapping("/{id}")
    public ResponseEntity<EtudiantResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(etudiantService.getEtudiantById(id));
    }

    // PUT /api/etudiants/{id}
    @PutMapping("/{id}")
    public ResponseEntity<EtudiantResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EtudiantRequestDTO dto) {
        return ResponseEntity.ok(etudiantService.updateEtudiant(id, dto));
    }

    // DELETE /api/etudiants/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        etudiantService.deleteEtudiant(id);
        return ResponseEntity.noContent().build();
}
}
