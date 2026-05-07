package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.EvenementRequestDTO;
import tn.esprit.espritconnect2.DTO.EvenementResponseDTO;
import tn.esprit.espritconnect2.Service.IEvenementService;

import java.util.List;

@RestController
@RequestMapping("/api/evenements")
@RequiredArgsConstructor
public class EvenementController {

    private final IEvenementService evenementService;

    @PostMapping
    public ResponseEntity<EvenementResponseDTO> create(@Valid @RequestBody EvenementRequestDTO dto) {
        return new ResponseEntity<>(evenementService.createEvenement(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EvenementResponseDTO>> getAll() {
        return ResponseEntity.ok(evenementService.getAllEvenements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvenementResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(evenementService.getEvenementById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvenementResponseDTO> update(@PathVariable Long id, @Valid @RequestBody EvenementRequestDTO dto) {
        return ResponseEntity.ok(evenementService.updateEvenement(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        evenementService.deleteEvenement(id);
        return ResponseEntity.noContent().build();
    }
}
