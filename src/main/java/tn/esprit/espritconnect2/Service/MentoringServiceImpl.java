package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.MentoringRequestDTO;
import tn.esprit.espritconnect2.DTO.MentoringResponseDTO;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.Repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentoringServiceImpl implements IMentoringService {

    private final MentoringRepository mentoringRepository;
    private final EtudiantRepository etudiantRepository;
    private final AlumniRepository alumniRepository;

    private MentoringResponseDTO toDTO(Mentoring mentoring) {

        return MentoringResponseDTO.builder()
                .idMentoring(mentoring.getIdMentoring())
                .dateDebut(mentoring.getDateDebut())
                .dateFin(mentoring.getDateFin())
                .statutMentoring(mentoring.getStatutMentoring())
                .domaine(mentoring.getDomaine())
                .objectifs(mentoring.getObjectifs())
                .nomEtudiant(mentoring.getEtudiant().getNom())
                .nomAlumni(mentoring.getAlumni().getNom())
                .build();
    }

    @Override
    public MentoringResponseDTO ajouterMentoring(MentoringRequestDTO dto) {

        Etudiant etudiant = etudiantRepository.findById(dto.getEtudiantId())
                .orElseThrow(() -> new RuntimeException("Etudiant introuvable"));

        Alumni alumni = alumniRepository.findById(dto.getAlumniId())
                .orElseThrow(() -> new RuntimeException("Alumni introuvable"));

        Mentoring mentoring = new Mentoring();

        mentoring.setDateDebut(dto.getDateDebut());
        mentoring.setDateFin(dto.getDateFin());
        mentoring.setStatutMentoring(dto.getStatutMentoring());
        mentoring.setDomaine(dto.getDomaine());
        mentoring.setObjectifs(dto.getObjectifs());

        mentoring.setEtudiant(etudiant);
        mentoring.setAlumni(alumni);

        return toDTO(mentoringRepository.save(mentoring));
    }

    @Override
    public List<MentoringResponseDTO> getAllMentorings() {

        return mentoringRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MentoringResponseDTO getMentoringById(Long id) {

        Mentoring mentoring = mentoringRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mentoring introuvable"));

        return toDTO(mentoring);
    }

    @Override
    public void deleteMentoring(Long id) {

        mentoringRepository.deleteById(id);
    }
}