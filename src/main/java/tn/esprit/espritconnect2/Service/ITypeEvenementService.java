package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.TypeEvenementDTO;

import java.util.List;

public interface ITypeEvenementService {
    List<TypeEvenementDTO> getAll();
    List<TypeEvenementDTO> getActive();
    TypeEvenementDTO getById(Long id);
    TypeEvenementDTO create(TypeEvenementDTO dto);
    TypeEvenementDTO update(Long id, TypeEvenementDTO dto);
    void delete(Long id);
}
