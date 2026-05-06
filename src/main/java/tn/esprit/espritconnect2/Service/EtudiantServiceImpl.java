package tn.esprit.espritconnect2.Service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.EtudiantRequestDTO;
import tn.esprit.espritconnect2.DTO.EtudiantResponseDTO;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class EtudiantServiceImpl {

    private final EtudiantRepository etudiantRepository;

    // ─── Mapper DTO → Entité ──────────────────────────────────────────────────
    private Etudiant toEntity(EtudiantRequestDTO dto) {
        Etudiant e = new Etudiant();
        e.setNom(dto.getNom());
        e.setEmail(dto.getEmail());
        e.setPassword(dto.getPassword());
        e.setNiveau(dto.getNiveau());
        e.setFiliere(dto.getFiliere());
        e.setScoreReadiness(dto.getScoreReadiness());
        return e;
    }

    // ─── Mapper Entité → ResponseDTO ─────────────────────────────────────────
    private EtudiantResponseDTO toDTO(Etudiant e) {
        return EtudiantResponseDTO.builder()
                .idEtudiant(e.getIdEtudiant())
                .nom(e.getNom())
                .email(e.getEmail())
                .niveau(e.getNiveau())
                .filiere(e.getFiliere())
                .scoreReadiness(e.getScoreReadiness())
                .dateInscription(e.getDateInscription())
                .build();
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────
    public EtudiantResponseDTO ajouterEtudiant(EtudiantRequestDTO dto) {
        // Validation métier : email unique
        if (etudiantRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un étudiant avec cet email existe déjà : " + dto.getEmail());
        }

        Etudiant etudiant = toEntity(dto);
        etudiant.setDateInscription(new Date()); // Date auto à la création
        Etudiant saved = etudiantRepository.save(etudiant);
        return toDTO(saved);
    }

    // ─── READ ALL ─────────────────────────────────────────────────────────────
    public List<EtudiantResponseDTO> getAllEtudiants() {
        return etudiantRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── READ BY ID ───────────────────────────────────────────────────────────
    public EtudiantResponseDTO getEtudiantById(Long id) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable avec l'id : " + id));
        return toDTO(etudiant);
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────
    public EtudiantResponseDTO updateEtudiant(Long id, EtudiantRequestDTO dto) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable avec l'id : " + id));

        // Vérifier unicité email seulement si l'email a changé
        if (!etudiant.getEmail().equals(dto.getEmail())
                && etudiantRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé : " + dto.getEmail());
        }

        etudiant.setNom(dto.getNom());
        etudiant.setEmail(dto.getEmail());
        etudiant.setNiveau(dto.getNiveau());
        etudiant.setFiliere(dto.getFiliere());
        etudiant.setScoreReadiness(dto.getScoreReadiness());
        // On ne met pas à jour le password ici → endpoint séparé recommandé

        return toDTO(etudiantRepository.save(etudiant));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────
    public void deleteEtudiant(Long id) {
        if (!etudiantRepository.existsById(id)) {
            throw new RuntimeException("Étudiant introuvable avec l'id : " + id);
        }
        etudiantRepository.deleteById(id);
    }
}
