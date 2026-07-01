package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.FichierRequestDTO;
import tn.esprit.espritconnect2.DTO.FichierResponseDTO;
import tn.esprit.espritconnect2.Service.IFichierService;

import java.util.List;

@RestController
@RequestMapping("/api/fichiers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FichierController {

    private final IFichierService fichierService;

    @PostMapping
    public ResponseEntity<FichierResponseDTO> create(@Valid @RequestBody FichierRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fichierService.create(dto));
    }

    @PostMapping("/{fichierId}/attach-candidature/{candidatureId}")
    public ResponseEntity<FichierResponseDTO> attachToCandidature(
            @PathVariable Long fichierId,
            @PathVariable Long candidatureId) {
        return ResponseEntity.ok(fichierService.attachToCandidature(fichierId, candidatureId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<FichierResponseDTO>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(fichierService.getByEtudiant(studentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fichierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
