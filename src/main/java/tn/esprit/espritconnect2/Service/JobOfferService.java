package tn.esprit.espritconnect2.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.JobOfferDTO;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.Repository.OffreRepository;
import tn.esprit.espritconnect2.Repository.EntrepriseRepository;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobOfferService {

    @Autowired
    private OffreRepository offreRepository;
    
    @Autowired
    private EntrepriseRepository entrepriseRepository;

    // Get all jobs with filtering and pagination
    public Map<String, Object> getJobs(String search, List<String> statuses, List<String> contractTypes,
                                       String department, String location, String sortBy, String sortOrder,
                                       int page, int limit) {
        return filterJobs(search, statuses, contractTypes, department, location, sortBy, sortOrder, page, limit, false);
    }

    // Get archived jobs only
    public Map<String, Object> getArchivedJobs(String search, String sortBy, String sortOrder, int page, int limit) {
        return filterJobs(search, null, null, null, null, sortBy, sortOrder, page, limit, true);
    }

    private Map<String, Object> filterJobs(String search, List<String> statuses, List<String> contractTypes,
                                             String department, String location, String sortBy, String sortOrder,
                                             int page, int limit, boolean archivedOnly) {
        List<Offre> allOffres = offreRepository.findAll();
        List<Offre> filteredOffres = allOffres.stream()
            .filter(offre -> archivedOnly
                ? Boolean.TRUE.equals(offre.getIsArchived())
                : !Boolean.TRUE.equals(offre.getIsArchived()))
            .filter(offre -> search == null || search.isEmpty() ||
                   offre.getTitre().toLowerCase().contains(search.toLowerCase()) ||
                   (offre.getDepartment() != null && offre.getDepartment().toLowerCase().contains(search.toLowerCase())) ||
                   (offre.getLocalisation() != null && offre.getLocalisation().toLowerCase().contains(search.toLowerCase())))
            .filter(offre -> archivedOnly || statuses == null || statuses.isEmpty() ||
                   statuses.contains(offre.getStatutOfrre() != null ? offre.getStatutOfrre().toString() : ""))
            .filter(offre -> contractTypes == null || contractTypes.isEmpty() ||
                   contractTypes.contains(offre.getTypeOffre() != null ? offre.getTypeOffre().toString() : ""))
            .filter(offre -> department == null || department.isEmpty() ||
                   (offre.getDepartment() != null && offre.getDepartment().equalsIgnoreCase(department)))
            .filter(offre -> location == null || location.isEmpty() ||
                   (offre.getLocalisation() != null && offre.getLocalisation().equalsIgnoreCase(location)))
            .sorted((a, b) -> compareOffres(a, b, sortBy, sortOrder))
            .collect(Collectors.toList());

        List<JobOfferDTO> dtos = filteredOffres.stream()
            .skip((long) (page - 1) * limit)
            .limit(limit)
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("data", dtos);
        response.put("total", filteredOffres.size());
        return response;
    }

    private int compareOffres(Offre a, Offre b, String sortBy, String sortOrder) {
        int direction = "asc".equalsIgnoreCase(sortOrder) ? 1 : -1;
        String field = sortBy != null ? sortBy : "recent";

        return switch (field) {
            case "title" -> direction * nullSafeString(a.getTitre()).compareToIgnoreCase(nullSafeString(b.getTitre()));
            case "deadline" -> direction * compareDates(a.getDeadline(), b.getDeadline());
            case "applications" -> direction * Integer.compare(
                a.getCandidatures() != null ? a.getCandidatures().size() : 0,
                b.getCandidatures() != null ? b.getCandidatures().size() : 0
            );
            default -> direction * compareDates(a.getDatePublication(), b.getDatePublication());
        };
    }

    private String nullSafeString(String value) {
        return value != null ? value : "";
    }

    private int compareDates(Date a, Date b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    // Get job by ID
    public JobOfferDTO getJobById(Long id) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        return convertToDTO(offre);
    }

    // Get jobs by enterprise
    public List<JobOfferDTO> getJobsByEnterprise(Long entrepriseId) {
        List<Offre> offres = offreRepository.findByEntreprise_IdEntrepriseOrderByDatePublicationDesc(entrepriseId);
        return offres.stream()
            .filter(offre -> !Boolean.TRUE.equals(offre.getIsArchived()))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Create job
    @Transactional
    public JobOfferDTO createJob(JobOfferDTO dto) {
        Offre offre = convertToEntity(dto);
        offre.setDatePublication(new Date());
        offre.setIsArchived(false);
        offre.setIsPinned(false);
        offre.setStatutOfrre(Status.valueOf(dto.getStatus() != null ? dto.getStatus() : "ACTIVE"));
        offre = offreRepository.save(offre);
        return convertToDTO(offre);
    }

    // Update job
    @Transactional
    public JobOfferDTO updateJob(Long id, JobOfferDTO dto) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        updateEntityFromDTO(offre, dto);
        offre = offreRepository.save(offre);
        return convertToDTO(offre);
    }

    // Delete job
    @Transactional
    public void deleteJob(Long id) {
        offreRepository.deleteById(id);
    }

    // Duplicate job
    @Transactional
    public JobOfferDTO duplicateJob(Long id) {
        Offre original = offreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        
        Offre duplicate = new Offre();
        duplicate.setTitre(original.getTitre() + " (Copie)");
        duplicate.setDescription(original.getDescription());
        duplicate.setTypeOffre(original.getTypeOffre());
        duplicate.setLocalisation(original.getLocalisation());
        duplicate.setDomaine(original.getDomaine());
        duplicate.setDepartment(original.getDepartment());
        duplicate.setExperienceLevel(original.getExperienceLevel());
        duplicate.setNumberOfPositions(original.getNumberOfPositions());
        duplicate.setWorkMode(original.getWorkMode());
        duplicate.setSalaryMin(original.getSalaryMin());
        duplicate.setSalaryMax(original.getSalaryMax());
        duplicate.setDuration(original.getDuration());
        duplicate.setDeadline(original.getDeadline());
        duplicate.setCompetencesRequises(new ArrayList<>(original.getCompetencesRequises()));
        duplicate.setTechnologies(new ArrayList<>(original.getTechnologies()));
        duplicate.setLanguages(new ArrayList<>(original.getLanguages()));
        duplicate.setResponsibilities(original.getResponsibilities());
        duplicate.setRequirements(original.getRequirements());
        duplicate.setBenefits(original.getBenefits());
        duplicate.setEntreprise(original.getEntreprise());
        duplicate.setStatutOfrre(Status.DRAFT);
        duplicate.setDatePublication(new Date());
        duplicate.setIsPinned(false);
        duplicate.setIsArchived(false);
        
        duplicate = offreRepository.save(duplicate);
        return convertToDTO(duplicate);
    }

    // Archive job
    @Transactional
    public JobOfferDTO archiveJob(Long id) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        offre.setIsArchived(true);
        offre.setStatutOfrre(Status.ARCHIVED);
        offre = offreRepository.save(offre);
        return convertToDTO(offre);
    }

    // Restore job
    @Transactional
    public JobOfferDTO restoreJob(Long id) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        offre.setIsArchived(false);
        offre.setStatutOfrre(Status.ACTIVE);
        offre = offreRepository.save(offre);
        return convertToDTO(offre);
    }

    // Pause applications
    @Transactional
    public JobOfferDTO pauseApplications(Long id) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        offre.setStatutOfrre(Status.PAUSED);
        offre = offreRepository.save(offre);
        return convertToDTO(offre);
    }

    // Resume applications
    @Transactional
    public JobOfferDTO resumeApplications(Long id) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        offre.setStatutOfrre(Status.ACTIVE);
        offre = offreRepository.save(offre);
        return convertToDTO(offre);
    }

    // Close job
    @Transactional
    public JobOfferDTO closeJob(Long id) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        offre.setStatutOfrre(Status.CLOSED);
        offre = offreRepository.save(offre);
        return convertToDTO(offre);
    }

    // Pin job
    @Transactional
    public JobOfferDTO pinJob(Long id) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        offre.setIsPinned(true);
        offre = offreRepository.save(offre);
        return convertToDTO(offre);
    }

    // Unpin job
    @Transactional
    public JobOfferDTO unpinJob(Long id) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        offre.setIsPinned(false);
        offre = offreRepository.save(offre);
        return convertToDTO(offre);
    }

    // Save draft
    @Transactional
    public JobOfferDTO saveDraft(JobOfferDTO dto) {
        Offre offre = convertToEntity(dto);
        offre.setStatutOfrre(Status.DRAFT);
        offre.setDatePublication(new Date());
        offre = offreRepository.save(offre);
        return convertToDTO(offre);
    }

    // Update draft
    @Transactional
    public JobOfferDTO updateDraft(Long id, JobOfferDTO dto) {
        return updateJob(id, dto);
    }

    private JobOfferDTO convertToDTO(Offre offre) {
        JobOfferDTO dto = new JobOfferDTO();
        dto.setId(offre.getIdOffre());
        dto.setTitle(offre.getTitre());
        dto.setDescription(offre.getDescription());
        dto.setContractType(offre.getTypeOffre() != null ? offre.getTypeOffre().toString() : null);
        dto.setLocation(offre.getLocalisation());
        dto.setDepartment(offre.getDepartment());
        dto.setExperienceLevel(offre.getExperienceLevel() != null ? offre.getExperienceLevel().toString() : null);
        dto.setNumberOfPositions(offre.getNumberOfPositions());
        dto.setWorkMode(offre.getWorkMode() != null ? offre.getWorkMode().toString() : null);
        dto.setSalaryMin(offre.getSalaryMin());
        dto.setSalaryMax(offre.getSalaryMax());
        dto.setDuration(offre.getDuration());
        dto.setDeadline(offre.getDeadline() != null ? offre.getDeadline().toString() : null);
        dto.setRequiredSkills(offre.getCompetencesRequises());
        dto.setTechnologies(offre.getTechnologies());
        dto.setLanguages(offre.getLanguages());
        dto.setResponsibilities(offre.getResponsibilities());
        dto.setRequirements(offre.getRequirements());
        dto.setBenefits(offre.getBenefits());
        dto.setStatus(offre.getStatutOfrre() != null ? offre.getStatutOfrre().toString() : "ACTIVE");
        dto.setApplicationCount(offre.getCandidatures() != null ? offre.getCandidatures().size() : 0);
        dto.setIsPinned(offre.getIsPinned());
        dto.setIsArchived(offre.getIsArchived());
        if (offre.getEntreprise() != null) {
            dto.setEntrepriseId(offre.getEntreprise().getIdEntreprise());
            dto.setCompanyName(offre.getEntreprise().getNom());
        }
        return dto;
    }

    private Offre convertToEntity(JobOfferDTO dto) {
        Offre offre = new Offre();
        updateEntityFromDTO(offre, dto);
        return offre;
    }

    private void updateEntityFromDTO(Offre offre, JobOfferDTO dto) {
        offre.setTitre(dto.getTitle());
        offre.setDescription(dto.getDescription());
        if (dto.getContractType() != null) {
            offre.setTypeOffre(Type.valueOf(dto.getContractType()));
        }
        offre.setLocalisation(dto.getLocation());
        offre.setDomaine(dto.getDepartment());
        offre.setDepartment(dto.getDepartment());
        if (dto.getExperienceLevel() != null) {
            offre.setExperienceLevel(ExperienceLevel.valueOf(dto.getExperienceLevel()));
        }
        offre.setNumberOfPositions(dto.getNumberOfPositions());
        if (dto.getWorkMode() != null) {
            offre.setWorkMode(WorkMode.valueOf(dto.getWorkMode()));
        }
        offre.setSalaryMin(dto.getSalaryMin());
        offre.setSalaryMax(dto.getSalaryMax());
        offre.setDuration(dto.getDuration());
        
        // Handle deadline conversion from String to Date
        if (dto.getDeadline() != null && !dto.getDeadline().isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                offre.setDeadline(dateFormat.parse(dto.getDeadline()));
            } catch (Exception e) {
                // If parsing fails, set deadline to null or throw exception
                e.printStackTrace();
            }
        }
        
        offre.setCompetencesRequises(dto.getRequiredSkills());
        offre.setTechnologies(dto.getTechnologies());
        offre.setLanguages(dto.getLanguages());
        offre.setResponsibilities(dto.getResponsibilities());
        offre.setRequirements(dto.getRequirements());
        offre.setBenefits(dto.getBenefits());
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElse(null);
            offre.setEntreprise(entreprise);
        }
    }
}
