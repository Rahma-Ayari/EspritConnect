package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.Entitie.Offre;
import tn.esprit.espritconnect2.Entitie.StatutOffre;
import tn.esprit.espritconnect2.Repository.OffreRepository;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
public class OffreController {

    private final OffreRepository offreRepository;

    @GetMapping("/public")
    public List<Offre> listPublic() {
        return offreRepository.findByStatutOffreOrderByDatePublicationDesc(StatutOffre.ACTIVE);
    }

    @GetMapping
    public List<Offre> listAll() {
        return offreRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offre> getById(@PathVariable Long id) {
        return offreRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENTREPRISE','ADMIN')")
    public ResponseEntity<Offre> create(@Valid @RequestBody Offre offre) {
        return ResponseEntity.status(201).body(offreRepository.save(offre));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENTREPRISE','ADMIN')")
    public ResponseEntity<Offre> update(@PathVariable Long id,
                                        @Valid @RequestBody Offre payload) {
        return offreRepository.findById(id).map(existing -> {
            existing.setTitre(payload.getTitre());
            existing.setDescription(payload.getDescription());
            existing.setTypeOffre(payload.getTypeOffre());
            existing.setLocalisation(payload.getLocalisation());
            existing.setStatutOffre(payload.getStatutOffre());
            return ResponseEntity.ok(offreRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENTREPRISE','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!offreRepository.existsById(id)) return ResponseEntity.notFound().build();
        offreRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
