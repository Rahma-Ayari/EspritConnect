package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.AlumniRequestDTO;
import tn.esprit.espritconnect2.DTO.AlumniResponseDTO;

import java.util.List;

public interface IAlumniService {

    AlumniResponseDTO ajouterAlumni(AlumniRequestDTO dto);

    List<AlumniResponseDTO> getAllAlumni();

    AlumniResponseDTO getAlumniById(Long id);

    AlumniResponseDTO updateAlumni(Long id, AlumniRequestDTO dto);

    void deleteAlumni(Long id);
}