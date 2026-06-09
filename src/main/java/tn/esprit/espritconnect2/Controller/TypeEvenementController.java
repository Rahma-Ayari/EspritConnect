package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.TypeEvenementDTO;
import tn.esprit.espritconnect2.Service.ITypeEvenementService;

import java.util.List;

@RestController
@RequestMapping("/api/evenement-types")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:2400"})
@RequiredArgsConstructor
public class TypeEvenementController {

    private final ITypeEvenementService service;

    @GetMapping
    public ResponseEntity<List<TypeEvenementDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<TypeEvenementDTO>> getActive() {
        return ResponseEntity.ok(service.getActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypeEvenementDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<TypeEvenementDTO> create(@Valid @RequestBody TypeEvenementDTO dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypeEvenementDTO> update(@PathVariable Long id, @Valid @RequestBody TypeEvenementDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
