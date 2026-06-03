package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.EvenementRequestDTO;
import tn.esprit.espritconnect2.DTO.EvenementResponseDTO;
import tn.esprit.espritconnect2.DTO.EvenementStatsDTO;
import tn.esprit.espritconnect2.DTO.ParticipationDTO;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.Exception.BusinessRuleException;
import tn.esprit.espritconnect2.Exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.EntrepriseRepository;
import tn.esprit.espritconnect2.Repository.EvenementRepository;
import tn.esprit.espritconnect2.Repository.ParticipationRepository;
import tn.esprit.espritconnect2.Repository.TypeEvenementRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EvenementServiceImpl implements IEvenementService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_UPCOMING = "UPCOMING";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final Set<String> VALID_STATUSES = Set.of(STATUS_ACTIVE, STATUS_UPCOMING, STATUS_CANCELLED, STATUS_COMPLETED);
    private static final List<String> PUBLIC_STATUSES = List.of(STATUS_ACTIVE, STATUS_UPCOMING);
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final EvenementRepository evenementRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final TypeEvenementRepository typeEvenementRepository;
    private final ParticipationRepository participationRepository;

    @Value("${app.upload.events-dir:uploads/events}")
    private String eventUploadDir;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    private Evenement toEntity(EvenementRequestDTO dto, User currentUser) {
        Evenement evenement = new Evenement();
        evenement.setOwner(currentUser);
        applyDto(evenement, dto);
        return evenement;
    }

    private void applyDto(Evenement evenement, EvenementRequestDTO dto) {
        validateDates(dto);

        TypeEvenement type = typeEvenementRepository.findById(dto.getTypeEvenementId())
                .orElseThrow(() -> new NotFoundException("Type d'evenement introuvable"));

        Entreprise entreprise = null;
        if (dto.getEntrepriseId() != null) {
            entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new NotFoundException("Entreprise introuvable"));
        }

        boolean unlimited = Boolean.TRUE.equals(dto.getUnlimitedParticipants());
        Integer capacity = unlimited ? null : dto.getCapacite();
        if (!unlimited && (capacity == null || capacity < 1)) {
            throw new BusinessRuleException("La capacite est obligatoire quand l'evenement est limite");
        }

        evenement.setTitre(dto.getTitre().trim());
        evenement.setLieu(dto.getLieu().trim());
        evenement.setDateDebut(dto.getDateDebut());
        evenement.setDateFin(dto.getDateFin());
        evenement.setHeureDebut(dto.getHeureDebut());
        evenement.setHeureFin(dto.getHeureFin());
        evenement.setDureeMinutes(calculateDurationMinutes(dto));
        evenement.setDateEvenement(toLegacyDate(dto));
        evenement.setCapacite(capacity);
        evenement.setUnlimitedParticipants(unlimited);
        evenement.setTypeEvenement(type);
        evenement.setType(type.getNom());
        evenement.setImageUrl(clean(dto.getImageUrl()));
        evenement.setStatus(normalizeStatus(dto.getStatus(), dto));
        evenement.setEntreprise(entreprise);
        if (evenement.getNombreParticipants() == null) {
            evenement.setNombreParticipants(0);
        }
    }

    private EvenementResponseDTO toDTO(Evenement evenement, User currentUser) {
        Entreprise entreprise = evenement.getEntreprise();
        TypeEvenement type = evenement.getTypeEvenement();
        User owner = evenement.getOwner();
        UUID currentUserId = currentUser != null ? currentUser.getId() : null;
        Integer capacity = evenement.getCapacite();
        Integer participants = evenement.getNombreParticipants() == null ? 0 : evenement.getNombreParticipants();
        boolean unlimited = Boolean.TRUE.equals(evenement.getUnlimitedParticipants());

        return EvenementResponseDTO.builder()
                .idEvenement(evenement.getIdEvenement())
                .titre(evenement.getTitre())
                .lieu(evenement.getLieu())
                .dateEvenement(evenement.getDateEvenement())
                .dateDebut(evenement.getDateDebut())
                .dateFin(evenement.getDateFin())
                .heureDebut(evenement.getHeureDebut())
                .heureFin(evenement.getHeureFin())
                .dureeMinutes(evenement.getDureeMinutes())
                .capacite(capacity)
                .unlimitedParticipants(unlimited)
                .nombreParticipants(participants)
                .placesRestantes(unlimited || capacity == null ? null : Math.max(capacity - participants, 0))
                .typeEvenementId(type != null ? type.getIdTypeEvenement() : null)
                .type(type != null ? type.getNom() : evenement.getType())
                .imageUrl(evenement.getImageUrl())
                .status(resolveStatus(evenement))
                .entrepriseId(entreprise != null ? entreprise.getIdEntreprise() : null)
                .entrepriseNom(entreprise != null ? entreprise.getNom() : null)
                .ownerId(owner != null ? owner.getId() : null)
                .ownerNom(owner != null ? owner.getNom() : null)
                .participated(currentUserId != null && participationRepository.existsByUserIdAndEvenementIdEvenement(currentUserId, evenement.getIdEvenement()))
                .ownedByCurrentUser(currentUserId != null && owner != null && owner.getId().equals(currentUserId))
                .build();
    }

    @Override
    public EvenementResponseDTO createEvenement(EvenementRequestDTO dto, User currentUser) {
        if (currentUser == null) {
            throw new BusinessRuleException("Utilisateur authentifie obligatoire");
        }
        return toDTO(evenementRepository.save(toEntity(dto, currentUser)), currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvenementResponseDTO> getAllEvenements(String search, String status, String type, User currentUser) {
        return evenementRepository.searchEvents(clean(search), clean(status), clean(type))
                .stream()
                .map(event -> toDTO(event, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvenementResponseDTO> getPublicEvenements(String search, String type, User currentUser) {
        return evenementRepository.searchPublicEvents(PUBLIC_STATUSES, clean(search), clean(type))
                .stream()
                .map(event -> toDTO(event, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvenementResponseDTO> getUpcomingEvenements(User currentUser) {
        return evenementRepository.findTop6ByDateEvenementAfterAndStatusInOrderByDateEvenementAsc(new Date(), PUBLIC_STATUSES)
                .stream()
                .map(event -> toDTO(event, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvenementResponseDTO> getMyCreatedEvenements(User currentUser) {
        requireUser(currentUser);
        return evenementRepository.findByOwnerIdOrderByDateDebutAscHeureDebutAsc(currentUser.getId())
                .stream()
                .map(event -> toDTO(event, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EvenementResponseDTO getEvenementById(Long id, User currentUser) {
        return toDTO(findEvenement(id), currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public EvenementResponseDTO getPublicEvenementById(Long id, User currentUser) {
        Evenement evenement = findEvenement(id);
        if (!PUBLIC_STATUSES.contains(resolveStatus(evenement))) {
            throw new NotFoundException("Evenement introuvable");
        }
        return toDTO(evenement, currentUser);
    }

    @Override
    public EvenementResponseDTO updateEvenement(Long id, EvenementRequestDTO dto, User currentUser) {
        Evenement evenement = findEvenement(id);
        ensureOwnerOrAdmin(evenement, currentUser);
        applyDto(evenement, dto);
        return toDTO(evenementRepository.save(evenement), currentUser);
    }

    @Override
    public void deleteEvenement(Long id, User currentUser) {
        Evenement evenement = findEvenement(id);
        ensureOwnerOrAdmin(evenement, currentUser);
        evenementRepository.delete(evenement);
    }

    @Override
    public ParticipationDTO participate(Long eventId, User currentUser) {
        requireUser(currentUser);
        Evenement evenement = findEvenement(eventId);
        String status = resolveStatus(evenement);
        if (!PUBLIC_STATUSES.contains(status)) {
            throw new BusinessRuleException("Participation impossible pour ce statut d'evenement");
        }
        if (participationRepository.existsByUserIdAndEvenementIdEvenement(currentUser.getId(), eventId)) {
            throw new BusinessRuleException("Vous participez deja a cet evenement");
        }
        if (!Boolean.TRUE.equals(evenement.getUnlimitedParticipants())
                && evenement.getCapacite() != null
                && safeParticipants(evenement) >= evenement.getCapacite()) {
            throw new BusinessRuleException("Nombre maximum de participants atteint");
        }

        Participation participation = Participation.builder()
                .user(currentUser)
                .evenement(evenement)
                .build();
        evenement.setNombreParticipants(safeParticipants(evenement) + 1);
        Participation saved = participationRepository.save(participation);
        return toParticipationDTO(saved, currentUser);
    }

    @Override
    public void cancelParticipation(Long eventId, User currentUser) {
        requireUser(currentUser);
        Participation participation = participationRepository.findByUserIdAndEvenementIdEvenement(currentUser.getId(), eventId)
                .orElseThrow(() -> new NotFoundException("Participation introuvable"));
        Evenement evenement = participation.getEvenement();
        participationRepository.delete(participation);
        evenement.setNombreParticipants(Math.max(safeParticipants(evenement) - 1, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationDTO> getMyParticipations(User currentUser) {
        requireUser(currentUser);
        return participationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(participation -> toParticipationDTO(participation, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Image obligatoire");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BusinessRuleException("Format image invalide");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessRuleException("Image trop volumineuse (max 5MB)");
        }

        String extension = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase(Locale.ROOT)
                : ".jpg";
        String fileName = UUID.randomUUID() + extension;
        try {
            Path uploadPath = Path.of(eventUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            file.transferTo(uploadPath.resolve(fileName));
            return contextPath + "/uploads/events/" + fileName;
        } catch (IOException ex) {
            throw new BusinessRuleException("Impossible d'enregistrer l'image");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countEvents() {
        return evenementRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public EvenementStatsDTO getStats() {
        Long totalCapacity = evenementRepository.sumCapacity();
        Long totalParticipants = evenementRepository.sumParticipants();
        double rate = totalCapacity == null || totalCapacity == 0 ? 0 : (totalParticipants * 100.0) / totalCapacity;

        return EvenementStatsDTO.builder()
                .totalEvents(evenementRepository.count())
                .activeEvents(evenementRepository.countByStatusIgnoreCase(STATUS_ACTIVE))
                .upcomingEvents(evenementRepository.countByStatusIgnoreCase(STATUS_UPCOMING))
                .cancelledEvents(evenementRepository.countByStatusIgnoreCase(STATUS_CANCELLED))
                .completedEvents(evenementRepository.countByStatusIgnoreCase(STATUS_COMPLETED))
                .totalCapacity(totalCapacity)
                .totalParticipants(totalParticipants)
                .participationRate(Math.round(rate * 10.0) / 10.0)
                .build();
    }

    private ParticipationDTO toParticipationDTO(Participation participation, User currentUser) {
        return ParticipationDTO.builder()
                .idParticipation(participation.getIdParticipation())
                .userId(participation.getUser().getId())
                .evenementId(participation.getEvenement().getIdEvenement())
                .createdAt(participation.getCreatedAt())
                .evenement(toDTO(participation.getEvenement(), currentUser))
                .build();
    }

    private void validateDates(EvenementRequestDTO dto) {
        if (dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new BusinessRuleException("La date de fin doit etre superieure ou egale a la date de debut");
        }
        LocalDateTime start = LocalDateTime.of(dto.getDateDebut(), dto.getHeureDebut());
        LocalDateTime end = LocalDateTime.of(dto.getDateFin(), dto.getHeureFin());
        if (!end.isAfter(start)) {
            throw new BusinessRuleException("La fin de l'evenement doit etre apres le debut");
        }
    }

    private Integer calculateDurationMinutes(EvenementRequestDTO dto) {
        return Math.toIntExact(Duration.between(
                LocalDateTime.of(dto.getDateDebut(), dto.getHeureDebut()),
                LocalDateTime.of(dto.getDateFin(), dto.getHeureFin())
        ).toMinutes());
    }

    private Date toLegacyDate(EvenementRequestDTO dto) {
        return Date.from(LocalDateTime.of(dto.getDateDebut(), dto.getHeureDebut())
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    private String resolveStatus(Evenement evenement) {
        String current = clean(evenement.getStatus());
        if (STATUS_CANCELLED.equalsIgnoreCase(current)) {
            return STATUS_CANCELLED;
        }
        if (evenement.getDateDebut() == null || evenement.getDateFin() == null || evenement.getHeureDebut() == null || evenement.getHeureFin() == null) {
            return normalizeStatus(current, null);
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(evenement.getDateDebut(), evenement.getHeureDebut());
        LocalDateTime end = LocalDateTime.of(evenement.getDateFin(), evenement.getHeureFin());
        if (now.isBefore(start)) return STATUS_UPCOMING;
        if (now.isAfter(end)) return STATUS_COMPLETED;
        return STATUS_ACTIVE;
    }

    private String normalizeStatus(String status, EvenementRequestDTO dto) {
        String normalized = clean(status);
        if (normalized == null) {
            if (dto == null) return STATUS_UPCOMING;
            LocalDate startDate = dto.getDateDebut();
            return startDate != null && startDate.isAfter(LocalDate.now()) ? STATUS_UPCOMING : STATUS_ACTIVE;
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!VALID_STATUSES.contains(normalized)) {
            throw new BusinessRuleException("Statut evenement invalide: " + status);
        }
        return normalized;
    }

    private Evenement findEvenement(Long id) {
        return evenementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Evenement introuvable"));
    }

    private void ensureOwnerOrAdmin(Evenement evenement, User currentUser) {
        requireUser(currentUser);
        boolean admin = currentUser.getRole() == Role.ADMIN;
        boolean owner = evenement.getOwner() != null && evenement.getOwner().getId().equals(currentUser.getId());
        if (!admin && !owner) {
            throw new BusinessRuleException("Vous ne pouvez modifier que vos propres evenements");
        }
    }

    private void requireUser(User currentUser) {
        if (currentUser == null) {
            throw new BusinessRuleException("Utilisateur authentifie obligatoire");
        }
    }

    private int safeParticipants(Evenement evenement) {
        return evenement.getNombreParticipants() == null ? 0 : evenement.getNombreParticipants();
    }

    private String clean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
