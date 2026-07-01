package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.FichierRequestDTO;
import tn.esprit.espritconnect2.DTO.FichierResponseDTO;
import tn.esprit.espritconnect2.Entitie.Alumni;
import tn.esprit.espritconnect2.Entitie.Candidature;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Fichier;
import tn.esprit.espritconnect2.Entitie.Type;
import tn.esprit.espritconnect2.exception.BusinessRuleException;
import tn.esprit.espritconnect2.exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.AlumniRepository;
import tn.esprit.espritconnect2.Repository.CandidatureRepository;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.FichierRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FichierServiceImpl implements IFichierService {

    private static final long MAX_CV_SIZE = 10L * 1024 * 1024; // 10MB

    private final FichierRepository fichierRepository;
    private final EtudiantRepository etudiantRepository;
    private final AlumniRepository alumniRepository;
    private final CandidatureRepository candidatureRepository;
    private final StudentAccountResolver studentAccountResolver;

    @Value("${app.upload.root}")
    private String uploadRoot;

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
    public FichierResponseDTO uploadForEmail(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Aucun fichier fourni");
        }
        if (file.getSize() > MAX_CV_SIZE) {
            throw new BusinessRuleException("Le fichier depasse la taille maximale de 10 Mo");
        }
        Etudiant etudiant = studentAccountResolver.resolveOrProvision(email);

        String original = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "cv");
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot).toLowerCase();
        }
        String storedName = "cv_" + etudiant.getIdEtudiant() + "_" + UUID.randomUUID() + ext;

        try {
            Path dir = Paths.get(uploadRoot, "cv").toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessRuleException("Echec de l'enregistrement du fichier: " + e.getMessage());
        }

        Fichier fichier = new Fichier();
        fichier.setNom(original);
        fichier.setUrl("/uploads/cv/" + storedName);
        fichier.setTypeFichier(Type.CV);
        fichier.setTaille(file.getSize());
        fichier.setUserId(String.valueOf(etudiant.getIdEtudiant()));
        fichier.setEtudiant(etudiant);
        return toDTO(fichierRepository.save(fichier));
    }

    @Override
    public List<FichierResponseDTO> getByEmail(String email) {
        Etudiant etudiant = studentAccountResolver.resolveOrProvision(email);
        return fichierRepository.findByEtudiantIdEtudiant(etudiant.getIdEtudiant()).stream()
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
