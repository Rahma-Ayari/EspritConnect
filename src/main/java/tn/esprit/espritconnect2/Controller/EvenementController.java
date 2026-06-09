package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.EvenementRequestDTO;
import tn.esprit.espritconnect2.DTO.EvenementResponseDTO;
import tn.esprit.espritconnect2.DTO.EvenementStatsDTO;
import tn.esprit.espritconnect2.DTO.ParticipationDTO;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Service.IEvenementService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evenements")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:2400"})
@RequiredArgsConstructor
public class EvenementController {

    private final IEvenementService evenementService;

    @PostMapping
    public ResponseEntity<EvenementResponseDTO> create(@Valid @RequestBody EvenementRequestDTO dto, Authentication authentication) {
        return new ResponseEntity<>(evenementService.createEvenement(dto, currentUser(authentication)), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EvenementResponseDTO>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            Authentication authentication
    ) {
        return ResponseEntity.ok(evenementService.getAllEvenements(search, status, type, currentUser(authentication)));
    }

    @GetMapping("/public")
    public ResponseEntity<List<EvenementResponseDTO>> getPublicEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            Authentication authentication
    ) {
        return ResponseEntity.ok(evenementService.getPublicEvenements(search, type, currentUser(authentication)));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<EvenementResponseDTO> getPublicEventById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(evenementService.getPublicEvenementById(id, currentUser(authentication)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EvenementResponseDTO>> getUpcomingEvents(Authentication authentication) {
        return ResponseEntity.ok(evenementService.getUpcomingEvenements(currentUser(authentication)));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<EvenementResponseDTO>> getMyCreatedEvents(Authentication authentication) {
        return ResponseEntity.ok(evenementService.getMyCreatedEvenements(currentUser(authentication)));
    }

    @GetMapping("/participations/mine")
    public ResponseEntity<List<ParticipationDTO>> getMyParticipations(Authentication authentication) {
        return ResponseEntity.ok(evenementService.getMyParticipations(currentUser(authentication)));
    }

    @PostMapping("/{id}/participations")
    public ResponseEntity<ParticipationDTO> participate(@PathVariable Long id, Authentication authentication) {
        return new ResponseEntity<>(evenementService.participate(id, currentUser(authentication)), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/participations")
    public ResponseEntity<Void> cancelParticipation(@PathVariable Long id, Authentication authentication) {
        evenementService.cancelParticipation(id, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/images")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("imageUrl", evenementService.uploadImage(file)));
    }

    @GetMapping("/stats")
    public ResponseEntity<EvenementStatsDTO> getStats() {
        return ResponseEntity.ok(evenementService.getStats());
    }

    @GetMapping("/stats/total")
    public ResponseEntity<Long> totalEvents() {
        return ResponseEntity.ok(evenementService.countEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvenementResponseDTO> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(evenementService.getEvenementById(id, currentUser(authentication)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvenementResponseDTO> update(@PathVariable Long id, @Valid @RequestBody EvenementRequestDTO dto, Authentication authentication) {
        return ResponseEntity.ok(evenementService.updateEvenement(id, dto, currentUser(authentication)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        evenementService.deleteEvenement(id, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return null;
        }
        return user;
    }
}
