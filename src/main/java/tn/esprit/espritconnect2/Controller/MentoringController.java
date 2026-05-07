package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.MentoringRequestDTO;
import tn.esprit.espritconnect2.DTO.MentoringResponseDTO;
import tn.esprit.espritconnect2.Service.MentoringServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/mentorings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MentoringController {

    private final MentoringServiceImpl mentoringService;

    @PostMapping
    public ResponseEntity<MentoringResponseDTO> ajouter(
            @Valid @RequestBody MentoringRequestDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mentoringService.ajouterMentoring(dto));
    }

    @GetMapping
    public ResponseEntity<List<MentoringResponseDTO>> getAll() {

        return ResponseEntity.ok(mentoringService.getAllMentorings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentoringResponseDTO> getById(@PathVariable Long id) {

        return ResponseEntity.ok(mentoringService.getMentoringById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        mentoringService.deleteMentoring(id);

        return ResponseEntity.noContent().build();
    }
}