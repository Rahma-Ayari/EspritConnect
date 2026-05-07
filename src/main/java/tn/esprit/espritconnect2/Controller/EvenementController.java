package tn.esprit.espritconnect2.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.Entitie.Evenement;
import tn.esprit.espritconnect2.Repository.EvenementRepository;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/evenements")
@RequiredArgsConstructor
public class EvenementController {

    private final EvenementRepository evenementRepository;

    @GetMapping("/upcoming")
    public List<Evenement> upcoming() {
        return evenementRepository.findByDateEvenementAfterOrderByDateEvenementAsc(new Date());
    }

    @GetMapping
    public List<Evenement> all() {
        return evenementRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENTREPRISE','ADMIN')")
    public ResponseEntity<Evenement> create(@RequestBody Evenement e) {
        return ResponseEntity.status(201).body(evenementRepository.save(e));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        evenementRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
