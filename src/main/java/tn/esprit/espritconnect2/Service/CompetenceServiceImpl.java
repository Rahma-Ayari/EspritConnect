package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.CompetenceRequestDTO;
import tn.esprit.espritconnect2.DTO.CompetenceResponseDTO;
import tn.esprit.espritconnect2.Entitie.Competence;
import tn.esprit.espritconnect2.Repository.CompetenceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompetenceServiceImpl implements ICompetenceService {

    private final CompetenceRepository competenceRepository;

    private Competence toEntity(CompetenceRequestDTO dto) {
        Competence c = new Competence();
        c.setLibelle(dto.getLibelle());
        c.setCategorie(dto.getCategorie());
        c.setNiveau(dto.getNiveau());
        return c;
    }

    private CompetenceResponseDTO toDTO(Competence c) {
        return CompetenceResponseDTO.builder()
                .idCompetence(c.getIdCompetence())
                .libelle(c.getLibelle())
                .categorie(c.getCategorie())
                .niveau(c.getNiveau())
                .build();
    }

    @Override
    public CompetenceResponseDTO ajouterCompetence(CompetenceRequestDTO dto) {
        Competence c = toEntity(dto);
        return toDTO(competenceRepository.save(c));
    }

    @Override
    public List<CompetenceResponseDTO> getAllCompetences() {
        return competenceRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CompetenceResponseDTO getCompetenceById(Long id) {
        Competence c = competenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compétence non trouvée avec l'id : " + id));
        return toDTO(c);
    }

    @Override
    @Transactional
    public CompetenceResponseDTO updateCompetence(Long id, CompetenceRequestDTO dto) {
        Competence c = competenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compétence non trouvée avec l'id : " + id));
        c.setLibelle(dto.getLibelle());
        c.setCategorie(dto.getCategorie());
        c.setNiveau(dto.getNiveau());
        return toDTO(competenceRepository.save(c));
    }

    @Override
    public void deleteCompetence(Long id) {
        competenceRepository.deleteById(id);
    }
}
