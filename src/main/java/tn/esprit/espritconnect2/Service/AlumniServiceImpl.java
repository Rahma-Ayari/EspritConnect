package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.AlumniRequestDTO;
import tn.esprit.espritconnect2.DTO.AlumniResponseDTO;
import tn.esprit.espritconnect2.Entitie.Alumni;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.AlumniRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlumniServiceImpl implements IAlumniService {

    private final AlumniRepository alumniRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // DTO → Entity
    private Alumni toEntity(AlumniRequestDTO dto) {

        Alumni alumni = new Alumni();

        alumni.setNom(dto.getNom());
        alumni.setEmail(dto.getEmail());
        alumni.setPassword(passwordEncoder.encode(dto.getPassword()));
        alumni.setAnneePromotion(dto.getAnneePromotion());
        alumni.setDomaine(dto.getDomaine());
        alumni.setDisponibleMentorat(dto.getDisponibleMentorat());
        alumni.setEntrepriseActuelle(dto.getEntrepriseActuelle());

        return alumni;
    }

    // Entity → DTO
    private AlumniResponseDTO toDTO(Alumni alumni) {

        return AlumniResponseDTO.builder()
                .idAlumni(alumni.getIdAlumni())
                .nom(alumni.getNom())
                .email(alumni.getEmail())
                .anneePromotion(alumni.getAnneePromotion())
                .domaine(alumni.getDomaine())
                .disponibleMentorat(alumni.getDisponibleMentorat())
                .entrepriseActuelle(alumni.getEntrepriseActuelle())
                .build();
    }

    // CREATE
    @Override
    public AlumniResponseDTO ajouterAlumni(AlumniRequestDTO dto) {

        if (alumniRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un alumni avec cet email existe déjà");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }

        Alumni alumni = toEntity(dto);

        Alumni saved = alumniRepository.save(alumni);

        userRepository.save(User.builder()
                .nom(saved.getNom())
                .email(saved.getEmail())
                .password(saved.getPassword())
                .role(Role.ALUMNI)
                .enabled(false)
                .inscriptionRefusee(false)
                .build());

        return toDTO(saved);
    }

    // READ ALL
    @Override
    public List<AlumniResponseDTO> getAllAlumni() {

        return alumniRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AlumniResponseDTO getAlumniByEmail(String email) {
        Alumni alumni = alumniRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Alumni introuvable pour cet email"));
        return toDTO(alumni);
    }

    // READ BY ID
    @Override
    public AlumniResponseDTO getAlumniById(Long id) {

        Alumni alumni = alumniRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumni introuvable"));

        return toDTO(alumni);
    }

    // UPDATE
    @Override
    public AlumniResponseDTO updateAlumni(Long id, AlumniRequestDTO dto) {

        Alumni alumni = alumniRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumni introuvable"));

        alumni.setNom(dto.getNom());
        alumni.setEmail(dto.getEmail());
        alumni.setAnneePromotion(dto.getAnneePromotion());
        alumni.setDomaine(dto.getDomaine());
        alumni.setDisponibleMentorat(dto.getDisponibleMentorat());
        alumni.setEntrepriseActuelle(dto.getEntrepriseActuelle());

        return toDTO(alumniRepository.save(alumni));
    }

    // DELETE
    @Override
    public void deleteAlumni(Long id) {

        if (!alumniRepository.existsById(id)) {
            throw new RuntimeException("Alumni introuvable");
        }

        alumniRepository.deleteById(id);
    }
}