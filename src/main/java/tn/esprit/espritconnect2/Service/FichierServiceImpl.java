package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.FichierRequestDTO;
import tn.esprit.espritconnect2.DTO.FichierResponseDTO;
import tn.esprit.espritconnect2.Entitie.Alumni;
import tn.esprit.espritconnect2.Entitie.Candidature;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Fichier;
import tn.esprit.espritconnect2.Exception.BusinessRuleException;
import tn.esprit.espritconnect2.Exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.AlumniRepository;
import tn.esprit.espritconnect2.Repository.CandidatureRepository;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.FichierRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FichierServiceImpl implements IFichierService {

    private final FichierRepository fichierRepository;
    private final EtudiantRepository etudiantRepository;
    private final AlumniRepository alumniRepository;
    private final CandidatureRepository candidatureRepository;

    @Override
    public FichierResponseDTO create(FichierRequestDTO dto) {
        Fichier fichier = new Fichier();
        fichier.setNom(dto.getNom());
        fichier.setUrl(dto.getUrl());
        fichier.setTypeFichier(dto.getTypeFichier());
        fichier.setTaille(dto.getTaille());

        if (dto.getEtudiantId() != null) {
            Etudiant etudiant = etudiantRepository.findById(dto.getEtudiantId())
                    .orElseThrow(() -> new NotFoundException("Etudiant introuvable avec id: " + dto.getEtudiantId()));
            fichier.setEtudiant(etudiant);
        }

        if (dto.getAlumniId() != null) {
            Alumni alumni = alumniRepository.findById(dto.getAlumniId())
                    .orElseThrow(() -> new NotFoundException("Alumni introuvable avec id: " + dto.getAlumniId()));
            fichier.setAlumni(alumni);
        }

        if (dto.getCandidatureId() != null) {
            Candidature candidature = candidatureRepository.findById(dto.getCandidatureId())
                    .orElseThrow(() -> new NotFoundException("Candidature introuvable avec id: " + dto.getCandidatureId()));
            fichier.setCandidature(candidature);
        }

        return toDTO(fichierRepository.save(fichier));
    }

    @Override
    public FichierResponseDTO attachToCandidature(Long fichierId, Long candidatureId) {
        Fichier fichier = fichierRepository.findById(fichierId)
                .orElseThrow(() -> new NotFoundException("Fichier introuvable avec id: " + fichierId));
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new NotFoundException("Candidature introuvable avec id: " + candidatureId));

        fichierRepository.findByCandidatureId(candidatureId).ifPresent(existing -> {
            if (!existing.getIdFichier().equals(fichierId)) {
                throw new BusinessRuleException("Cette candidature est deja associee a un autre fichier");
            }
        });

        fichier.setCandidature(candidature);
        return toDTO(fichierRepository.save(fichier));
    }

    @Override
    public List<FichierResponseDTO> getByEtudiant(Long etudiantId) {
        if (!etudiantRepository.existsById(etudiantId)) {
            throw new NotFoundException("Etudiant introuvable avec id: " + etudiantId);
        }

        return fichierRepository.findByEtudiantIdEtudiant(etudiantId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public void delete(Long fichierId) {
        Fichier fichier = fichierRepository.findById(fichierId)
                .orElseThrow(() -> new NotFoundException("Fichier introuvable avec id: " + fichierId));
        fichierRepository.delete(fichier);
    }

    private FichierResponseDTO toDTO(Fichier fichier) {
        return FichierResponseDTO.builder()
                .idFichier(fichier.getIdFichier())
                .nom(fichier.getNom())
                .url(fichier.getUrl())
                .typeFichier(fichier.getTypeFichier())
                .taille(fichier.getTaille())
                .etudiantId(fichier.getEtudiant() != null ? fichier.getEtudiant().getIdEtudiant() : null)
                .alumniId(fichier.getAlumni() != null ? fichier.getAlumni().getIdAlumni() : null)
                .candidatureId(fichier.getCandidature() != null ? fichier.getCandidature().getId() : null)
                .build();
    }
}
