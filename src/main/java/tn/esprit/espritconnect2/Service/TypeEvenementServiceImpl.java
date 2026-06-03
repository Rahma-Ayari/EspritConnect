package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.TypeEvenementDTO;
import tn.esprit.espritconnect2.Entitie.TypeEvenement;
import tn.esprit.espritconnect2.Exception.BusinessRuleException;
import tn.esprit.espritconnect2.Exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.TypeEvenementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeEvenementServiceImpl implements ITypeEvenementService {

    private final TypeEvenementRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<TypeEvenementDTO> getAll() {
        return repository.findAllByOrderByNomAsc().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypeEvenementDTO> getActive() {
        return repository.findByActifTrueOrderByNomAsc().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TypeEvenementDTO getById(Long id) {
        return toDTO(find(id));
    }

    @Override
    public TypeEvenementDTO create(TypeEvenementDTO dto) {
        String nom = clean(dto.getNom());
        if (repository.existsByNomIgnoreCase(nom)) {
            throw new BusinessRuleException("Ce type d'evenement existe deja");
        }
        TypeEvenement entity = TypeEvenement.builder()
                .nom(nom)
                .description(clean(dto.getDescription()))
                .actif(dto.getActif() == null || dto.getActif())
                .build();
        return toDTO(repository.save(entity));
    }

    @Override
    public TypeEvenementDTO update(Long id, TypeEvenementDTO dto) {
        TypeEvenement entity = find(id);
        String nom = clean(dto.getNom());
        repository.findByNomIgnoreCase(nom)
                .filter(existing -> !existing.getIdTypeEvenement().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Ce type d'evenement existe deja");
                });
        entity.setNom(nom);
        entity.setDescription(clean(dto.getDescription()));
        entity.setActif(dto.getActif() == null || dto.getActif());
        return toDTO(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Type d'evenement introuvable");
        }
        repository.deleteById(id);
    }

    private TypeEvenement find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Type d'evenement introuvable"));
    }

    private TypeEvenementDTO toDTO(TypeEvenement entity) {
        return TypeEvenementDTO.builder()
                .idTypeEvenement(entity.getIdTypeEvenement())
                .nom(entity.getNom())
                .description(entity.getDescription())
                .actif(entity.getActif())
                .build();
    }

    private String clean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
