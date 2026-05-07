package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import tn.esprit.espritconnect2.DTO.AICareerAssistantRequestDTO;
import tn.esprit.espritconnect2.DTO.AICareerAssistantResponseDTO;

import tn.esprit.espritconnect2.Entitie.*;

import tn.esprit.espritconnect2.Repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AICareerAssistantServiceImpl
        implements IAICareerAssistantService {

    private final AICareerAssistantRepository repository;

    private final EtudiantRepository etudiantRepository;

    private final OffreRepository offreRepository;

    private final AlumniRepository alumniRepository;

    private final MatchingRepository matchingRepository;

    // =====================================================
    // DTO → ENTITY
    // =====================================================

    private AICareerAssistant toEntity(
            AICareerAssistantRequestDTO dto
    ) {

        AICareerAssistant assistant =
                new AICareerAssistant();

        assistant.setModele(dto.getModele());

        assistant.setFonctionnalite(
                dto.getFonctionnalite()
        );

        assistant.setVersionAssistant(
                dto.getVersionAssistant()
        );

        assistant.setActif(dto.getActif());

        assistant.setScorePrecision(
                dto.getScorePrecision()
        );

        return assistant;
    }

    // =====================================================
    // ENTITY → DTO
    // =====================================================

    private AICareerAssistantResponseDTO toDTO(
            AICareerAssistant assistant
    ) {

        return AICareerAssistantResponseDTO
                .builder()
                .idAssistant(
                        assistant.getIdAssistant()
                )
                .modele(
                        assistant.getModele()
                )
                .fonctionnalite(
                        assistant.getFonctionnalite()
                )
                .versionAssistant(
                        assistant.getVersionAssistant()
                )
                .actif(
                        assistant.getActif()
                )
                .scorePrecision(
                        assistant.getScorePrecision()
                )
                .build();
    }

    // =====================================================
    // CRUD
    // =====================================================

    @Override
    public AICareerAssistantResponseDTO ajouterAssistant(
            AICareerAssistantRequestDTO dto
    ) {

        return toDTO(
                repository.save(
                        toEntity(dto)
                )
        );
    }

    @Override
    public List<AICareerAssistantResponseDTO>
    getAllAssistants() {

        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AICareerAssistantResponseDTO
    getAssistantById(Long id) {

        AICareerAssistant assistant =
                repository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assistant IA introuvable"
                                )
                        );

        return toDTO(assistant);
    }

    @Override
    public AICareerAssistantResponseDTO
    updateAssistant(
            Long id,
            AICareerAssistantRequestDTO dto
    ) {

        AICareerAssistant assistant =
                repository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assistant IA introuvable"
                                )
                        );

        assistant.setModele(dto.getModele());

        assistant.setFonctionnalite(
                dto.getFonctionnalite()
        );

        assistant.setVersionAssistant(
                dto.getVersionAssistant()
        );

        assistant.setActif(dto.getActif());

        assistant.setScorePrecision(
                dto.getScorePrecision()
        );

        return toDTO(
                repository.save(assistant)
        );
    }

    @Override
    public void deleteAssistant(Long id) {

        if (!repository.existsById(id)) {

            throw new RuntimeException(
                    "Assistant IA introuvable"
            );
        }

        repository.deleteById(id);
    }

    // =====================================================
    // 1. JOB MATCHING
    // =====================================================

    public Matching calculerMatchingJob(
            Long etudiantId,
            Long offreId
    ) {

        Etudiant etudiant =
                etudiantRepository.findById(etudiantId)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Etudiant introuvable"
                                )
                        );

        Offre offre =
                offreRepository.findById(offreId)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Offre introuvable"
                                )
                        );

        int score = 0;

        List<String> recommandations =
                new ArrayList<>();

        List<String> competencesRequises =
                new ArrayList<>();

        for (Competence competence :
                etudiant.getCompetences()) {

            if (offre.getDescription()
                    .toLowerCase()
                    .contains(
                            competence.getLibelle()
                                    .toLowerCase()
                    )) {

                score += 20;

                competencesRequises.add(
                        competence.getLibelle()
                );

            } else {

                recommandations.add(
                        "Améliorer compétence : "
                                + competence.getLibelle()
                );
            }
        }

        Matching matching = new Matching();

        matching.setDateCalcul(new Date());

        matching.setEtudiant(etudiant);

        matching.setOffre(offre);

        matching.setTypeMatching(
                "JOB_MATCHING"
        );

        matching.setScoreCompatibilite(
                (float) score
        );

        matching.setCompetencesRequises(
                competencesRequises
        );

        matching.setRecommandations(
                recommandations
        );

        return matchingRepository.save(matching);
    }

    // =====================================================
    // 2. MENTOR MATCHING
    // =====================================================

    public List<Alumni> recommanderMentors(
            Long etudiantId
    ) {

        Etudiant etudiant =
                etudiantRepository.findById(etudiantId)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Etudiant introuvable"
                                )
                        );

        return alumniRepository.findAll()

                .stream()

                .filter(alumni ->
                        alumni.getDisponibleMentorat()
                )

                .filter(alumni ->
                        alumni.getDomaine()
                                .equalsIgnoreCase(
                                        etudiant.getFiliere()
                                )
                )

                .collect(Collectors.toList());
    }

    // =====================================================
    // 3. CV IMPROVEMENT
    // =====================================================

    public List<String> analyserCV(
            Fichier fichier
    ) {

        List<String> recommandations =
                new ArrayList<>();

        if (fichier.getTaille() < 10000) {

            recommandations.add(
                    "Votre CV semble trop court"
            );
        }

        if (fichier.getNom() == null
                || fichier.getNom().isEmpty()) {

            recommandations.add(
                    "Ajoutez un nom de fichier valide"
            );
        }

        if (fichier.getUrl() == null) {

            recommandations.add(
                    "CV manquant"
            );
        }

        return recommandations;
    }

    // =====================================================
    // 4. PROFILE IMPROVEMENT
    // =====================================================

    public List<String> analyserProfil(
            Profil profil
    ) {

        List<String> recommandations =
                new ArrayList<>();

        if (profil.getBio() == null
                || profil.getBio().isEmpty()) {

            recommandations.add(
                    "Ajoutez une bio"
            );
        }

        if (profil.getLienGitHub() == null
                || profil.getLienGitHub().isEmpty()) {

            recommandations.add(
                    "Ajoutez votre GitHub"
            );
        }

        if (profil.getLienLinkedIn() == null
                || profil.getLienLinkedIn().isEmpty()) {

            recommandations.add(
                    "Ajoutez votre LinkedIn"
            );
        }

        return recommandations;
    }

    // =====================================================
    // 5. SELECT TOP CANDIDATES
    // =====================================================

    public List<Etudiant> topCandidats() {

        return etudiantRepository.findAll()

                .stream()

                .sorted((e1, e2) ->

                        Integer.compare(
                                e2.getScoreReadiness(),
                                e1.getScoreReadiness()
                        )
                )

                .limit(5)

                .collect(Collectors.toList());
    }
}