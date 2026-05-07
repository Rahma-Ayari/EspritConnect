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

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    private final FichierRepository fichierRepository;
    private final EtudiantRepository etudiantRepository;
    private final AlumniRepository alumniRepository;
    private final CandidatureRepository candidatureRepository;

    @Override
    public FichierResponseDTO create(FichierRequestDTO dto) {
        validateFile(dto.getNom(), dto.getTaille());

        if (dto.getEtudiantId() == null && dto.getAlumniId() == null) {
            throw new BusinessRuleException("Un fichier doit appartenir a un etudiant ou un alumni");
        }

        Fichier fichier = new Fichier();
        fichier.setNom(dto.getNom());
        fichier.setUrl(dto.getUrl());
        fichier.setTypeFichier(dto.getTypeFichier());
        fichier.setTaille(dto.getTaille());

        if (dto.getEtudiantId() != null) {
            Etudiant etudiant = etudiantRepository.findById(dto.getEtudiantId())
                    .orElseThrow(() -> new NotFoundException("Etudiant introuvable avec id: " + dto.getEtudiantId()));
            fichier.setEtudiant(etudiant);
            fichier.setUserId(String.valueOf(etudiant.getIdEtudiant()));
        }

        if (dto.getAlumniId() != null) {
            Alumni alumni = alumniRepository.findById(dto.getAlumniId())
                    .orElseThrow(() -> new NotFoundException("Alumni introuvable avec id: " + dto.getAlumniId()));
            fichier.setAlumni(alumni);
            fichier.setUserId(String.valueOf(alumni.getIdAlumni()));
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

    private void validateFile(String fileName, Long size) {
        if (size > MAX_FILE_SIZE) {
            throw new BusinessRuleException("Fichier trop volumineux. Limite: 10MB");
        }

        String lower = fileName.toLowerCase();
        if (!(lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx"))) {
            throw new BusinessRuleException("Extension invalide. Autorise: .pdf, .doc, .docx");
        }
    }

    private FichierResponseDTO toDTO(Fichier f) {
        return FichierResponseDTO.builder()
                .idFichier(f.getIdFichier())
                .nom(f.getNom())
                .url(f.getUrl())
                .typeFichier(f.getTypeFichier())
                .taille(f.getTaille())
                .etudiantId(f.getEtudiant() != null ? f.getEtudiant().getIdEtudiant() : null)
                .alumniId(f.getAlumni() != null ? f.getAlumni().getIdAlumni() : null)
                .candidatureId(f.getCandidature() != null ? f.getCandidature().getId() : null)
                .build();
    }
}
