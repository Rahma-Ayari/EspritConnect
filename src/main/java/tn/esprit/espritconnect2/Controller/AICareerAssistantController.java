package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import tn.esprit.espritconnect2.DTO.AICareerAssistantRequestDTO;
import tn.esprit.espritconnect2.DTO.AICareerAssistantResponseDTO;

import tn.esprit.espritconnect2.Entitie.*;

import tn.esprit.espritconnect2.Service.AICareerAssistantServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/assistants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AICareerAssistantController {

    private final AICareerAssistantServiceImpl assistantService;

    // =====================================================
    // CRUD
    // =====================================================

    @PostMapping
    public ResponseEntity<AICareerAssistantResponseDTO>
    ajouter(
            @Valid
            @RequestBody
            AICareerAssistantRequestDTO dto
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        assistantService
                                .ajouterAssistant(dto)
                );
    }

    @GetMapping
    public ResponseEntity
            <List<AICareerAssistantResponseDTO>>
    getAll() {

        return ResponseEntity.ok(
                assistantService.getAllAssistants()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity
            <AICareerAssistantResponseDTO>
    getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                assistantService
                        .getAssistantById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity
            <AICareerAssistantResponseDTO>
    update(
            @PathVariable Long id,

            @Valid
            @RequestBody
            AICareerAssistantRequestDTO dto
    ) {

        return ResponseEntity.ok(
                assistantService
                        .updateAssistant(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        assistantService.deleteAssistant(id);

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // 1. JOB MATCHING
    // =====================================================

    @PostMapping(
            "/matching-job/{etudiantId}/{offreId}"
    )
    public ResponseEntity<Matching>
    calculerMatchingJob(

            @PathVariable Long etudiantId,

            @PathVariable Long offreId
    ) {

        return ResponseEntity.ok(

                assistantService.calculerMatchingJob(
                        etudiantId,
                        offreId
                )
        );
    }

    // =====================================================
    // 2. MENTOR MATCHING
    // =====================================================

    @GetMapping(
            "/mentors/{etudiantId}"
    )
    public ResponseEntity<List<Alumni>>
    recommanderMentors(

            @PathVariable Long etudiantId
    ) {

        return ResponseEntity.ok(

                assistantService
                        .recommanderMentors(
                                etudiantId
                        )
        );
    }

    // =====================================================
    // 3. CV IMPROVEMENT
    // =====================================================

    @PostMapping("/analyse-cv")
    public ResponseEntity<List<String>>
    analyserCV(

            @RequestBody Fichier fichier
    ) {

        return ResponseEntity.ok(

                assistantService
                        .analyserCV(fichier)
        );
    }

    // =====================================================
    // 4. PROFILE IMPROVEMENT
    // =====================================================

    @PostMapping("/analyse-profil")
    public ResponseEntity<List<String>>
    analyserProfil(

            @RequestBody Profil profil
    ) {

        return ResponseEntity.ok(

                assistantService
                        .analyserProfil(profil)
        );
    }

    // =====================================================
    // 5. TOP CANDIDATES
    // =====================================================

    @GetMapping("/top-candidats")
    public ResponseEntity<List<Etudiant>>
    topCandidats() {

        return ResponseEntity.ok(

                assistantService.topCandidats()
        );
    }
}