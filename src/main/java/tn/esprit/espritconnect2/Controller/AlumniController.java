package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.AlumniRequestDTO;
import tn.esprit.espritconnect2.DTO.AlumniResponseDTO;
import tn.esprit.espritconnect2.Service.AlumniServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/alumni")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlumniController {

    private final AlumniServiceImpl alumniService;

    @PostMapping
    public ResponseEntity<AlumniResponseDTO> ajouter(
            @Valid @RequestBody AlumniRequestDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(alumniService.ajouterAlumni(dto));
    }

    @GetMapping
    public ResponseEntity<List<AlumniResponseDTO>> getAll() {

        return ResponseEntity.ok(alumniService.getAllAlumni());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlumniResponseDTO> getById(@PathVariable Long id) {

        return ResponseEntity.ok(alumniService.getAlumniById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlumniResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AlumniRequestDTO dto) {

        return ResponseEntity.ok(alumniService.updateAlumni(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        alumniService.deleteAlumni(id);

        return ResponseEntity.noContent().build();
    }
}