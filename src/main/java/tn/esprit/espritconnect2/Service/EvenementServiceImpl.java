package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.DeleteEventEmailRequest;
import tn.esprit.espritconnect2.DTO.EventSuggestionsDTO;
import tn.esprit.espritconnect2.DTO.EvenementRequestDTO;
import tn.esprit.espritconnect2.DTO.EvenementResponseDTO;
import tn.esprit.espritconnect2.DTO.EvenementStatsDTO;
import tn.esprit.espritconnect2.DTO.ParticipationDTO;
import tn.esprit.espritconnect2.DTO.WaitingListDTO;
import tn.esprit.espritconnect2.DTO.CategorySuggestion;
import tn.esprit.espritconnect2.DTO.EventMatch;
import tn.esprit.espritconnect2.Entitie.Entreprise;
import tn.esprit.espritconnect2.Entitie.Evenement;
import tn.esprit.espritconnect2.Entitie.ListeAttente;
import tn.esprit.espritconnect2.Entitie.Participation;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.TypeEvenement;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.exception.BusinessRuleException;
import tn.esprit.espritconnect2.exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.EntrepriseRepository;
import tn.esprit.espritconnect2.Repository.EvenementRepository;
import tn.esprit.espritconnect2.Repository.ListeAttenteRepository;
import tn.esprit.espritconnect2.Repository.ParticipationRepository;
import tn.esprit.espritconnect2.Repository.TypeEvenementRepository;

import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
    private final ListeAttenteRepository listeAttenteRepository;
    private final JavaMailSender mailSender;

    @Value("${app.upload.events-dir:uploads/events}")
    private String eventUploadDir;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${app.mail.from:}")
    private String fromEmail;

    @Value("${app.mail.from-name:EspritConnect}")
    private String fromName;

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
        boolean online = Boolean.TRUE.equals(dto.getOnline());
        Integer capacity = unlimited ? null : dto.getCapacite();
        if (!unlimited && (capacity == null || capacity < 1)) {
            throw new BusinessRuleException("La capacite est obligatoire quand l'evenement est limite");
        }
        if (!online && clean(dto.getLieu()) == null) {
            throw new BusinessRuleException("Le lieu est obligatoire pour les evenements physiques");
        }

        evenement.setTitre(dto.getTitre().trim());
        evenement.setLieu(online ? null : dto.getLieu().trim());
        evenement.setDateDebut(dto.getDateDebut());
        evenement.setDateFin(dto.getDateFin());
        evenement.setHeureDebut(dto.getHeureDebut());
        evenement.setHeureFin(dto.getHeureFin());
        evenement.setDureeMinutes(calculateDurationMinutes(dto));
        evenement.setDateEvenement(toLegacyDate(dto));
        evenement.setCapacite(capacity);
        evenement.setUnlimitedParticipants(unlimited);
        evenement.setOnline(online);
        evenement.setTypeEvenement(type);
        evenement.setType(type.getNom());
        evenement.setImageUrl(clean(dto.getImageUrl()));
        evenement.setLatitude(dto.getLatitude());
        evenement.setLongitude(dto.getLongitude());
        evenement.setStatus(resolveStatus(evenement));
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
                .online(Boolean.TRUE.equals(evenement.getOnline()))
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
                .latitude(evenement.getLatitude())
                .longitude(evenement.getLongitude())
                .build();
    }

    @Override
    public EvenementResponseDTO createEvenement(EvenementRequestDTO dto, User currentUser) {
        ensureAdmin(currentUser);
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
    public Page<EvenementResponseDTO> getAllEvenementsPaged(String search, String status, String type, User currentUser, Pageable pageable) {
        return evenementRepository.searchEvents(clean(search), clean(status), clean(type), pageable)
                .map(event -> toDTO(event, currentUser));
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
    public List<ParticipationDTO> getEventParticipations(Long eventId, User currentUser) {
        requireUser(currentUser);
        findEvenement(eventId);
        return participationRepository.findByEvenementIdEvenementOrderByCreatedAtDesc(eventId)
                .stream()
                .map(participation -> toParticipationDTO(participation, currentUser))
                .collect(Collectors.toList());
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
        ensureAdmin(currentUser);
        applyDto(evenement, dto);
        return toDTO(evenementRepository.save(evenement), currentUser);
    }

    @Override
    public void deleteEvenement(Long id, User currentUser) {
        deleteEvenement(id, currentUser, null);
    }

    @Override
    public void deleteEvenement(Long id, User currentUser, DeleteEventEmailRequest emailRequest) {
        Evenement evenement = findEvenement(id);
        ensureAdmin(currentUser);

        List<User> participants = participationRepository.findByEvenementIdEvenementOrderByCreatedAtDesc(id)
                .stream()
                .map(Participation::getUser)
                .collect(Collectors.toList());

        if (participants.isEmpty()) {
            evenementRepository.delete(evenement);
            return;
        }

        int total = participants.size();
        int sent = 0;

        for (User participant : participants) {
            try {
                String subject = emailRequest != null && emailRequest.getSubject() != null
                        ? emailRequest.getSubject()
                        : "Event cancelled: " + evenement.getTitre();
                String content = emailRequest != null && emailRequest.getContent() != null
                        ? emailRequest.getContent()
                        : "Hello,\n\nWe regret to inform you that the event '" + evenement.getTitre() + "' has been cancelled and removed by the administration.\n\nWe apologize for any inconvenience.\n\nBest regards,\nEsprit Connect Team";

                String htmlContent = "<p>" + content.replace("\n", "</p><p>") + "</p>";

                if (mailSender != null && fromEmail != null && !fromEmail.isBlank()) {
                    var message = mailSender.createMimeMessage();
                    var helper = new MimeMessageHelper(message, false, "UTF-8");
                    helper.setFrom(fromEmail, fromName);
                    helper.setTo(participant.getEmail());
                    helper.setSubject(subject);
                    helper.setText(htmlContent, true);
                    mailSender.send(message);
                }
                sent++;
            } catch (Exception ex) {
                // do not fail the whole deletion because of email errors
            }
        }

        if (sent < total) {
            // Log partial failure but continue with deletion
        }

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
        if (listeAttenteRepository.existsByUserIdAndEvenementIdEvenement(currentUser.getId(), eventId)) {
            throw new BusinessRuleException("Vous etes deja sur la liste d'attente pour cet evenement");
        }
        if (!Boolean.TRUE.equals(evenement.getUnlimitedParticipants())
                && evenement.getCapacite() != null
                && safeParticipants(evenement) >= evenement.getCapacite()) {
            throw new BusinessRuleException("Nombre maximum de participants atteint");
        }

        Participation participation = Participation.builder()
                .user(currentUser)
                .evenement(evenement)
                .status("PENDING_APPROVAL")
                .build();
        Participation saved = participationRepository.save(participation);
        return toParticipationDTO(saved, currentUser);
    }

    public ParticipationDTO approveParticipation(Long participationId, User currentUser) {
        requireUser(currentUser);
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new NotFoundException("Participation introuvable"));
        Evenement evenement = participation.getEvenement();
        ensureAdmin(currentUser);

        if (!"PENDING_APPROVAL".equalsIgnoreCase(participation.getStatus())) {
            throw new BusinessRuleException("Cette participation ne peut pas etre approuvee");
        }
        if (!Boolean.TRUE.equals(evenement.getUnlimitedParticipants())
                && evenement.getCapacite() != null
                && safeParticipants(evenement) >= evenement.getCapacite()) {
            throw new BusinessRuleException("L'evenement est complet, impossible d'approuver cette participation");
        }

        participation.setStatus("APPROVED");
        evenement.setNombreParticipants(safeParticipants(evenement) + 1);
        participationRepository.save(participation);
        return toParticipationDTO(participation, currentUser);
    }

    public void rejectParticipation(Long participationId, User currentUser) {
        requireUser(currentUser);
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new NotFoundException("Participation introuvable"));
        ensureAdmin(currentUser);

        if (!"PENDING_APPROVAL".equalsIgnoreCase(participation.getStatus())) {
            return;
        }

        Evenement evenement = participation.getEvenement();
        participationRepository.delete(participation);
        promoteFromWaitingListIfPossible(evenement.getIdEvenement());
    }

    @Override
    public void cancelParticipation(Long eventId, User currentUser) {
        requireUser(currentUser);
        Participation participation = participationRepository.findByUserIdAndEvenementIdEvenement(currentUser.getId(), eventId)
                .orElseThrow(() -> new NotFoundException("Participation introuvable"));
        Evenement evenement = participation.getEvenement();
        participationRepository.delete(participation);
        evenement.setNombreParticipants(Math.max(safeParticipants(evenement) - 1, 0));

        promoteFromWaitingListIfPossible(eventId);
    }

    @Override
    public WaitingListDTO joinWaitingList(Long eventId, User currentUser) {
        requireUser(currentUser);
        Evenement evenement = findEvenement(eventId);
        String status = resolveStatus(evenement);
        if (!PUBLIC_STATUSES.contains(status)) {
            throw new BusinessRuleException("Inscription a la liste d'attente impossible pour ce statut d'evenement");
        }
        if (participationRepository.existsByUserIdAndEvenementIdEvenement(currentUser.getId(), eventId)) {
            throw new BusinessRuleException("Vous participez deja a cet evenement");
        }
        if (listeAttenteRepository.existsByUserIdAndEvenementIdEvenement(currentUser.getId(), eventId)) {
            throw new BusinessRuleException("Vous etes deja sur la liste d'attente pour cet evenement");
        }
        if (!Boolean.TRUE.equals(evenement.getUnlimitedParticipants())
                && evenement.getCapacite() != null
                && safeParticipants(evenement) < evenement.getCapacite()) {
            throw new BusinessRuleException("Il reste des places disponibles, veuillez vous inscrire directement");
        }

        ListeAttente entry = ListeAttente.builder()
                .user(currentUser)
                .evenement(evenement)
                .build();
        ListeAttente saved = listeAttenteRepository.save(entry);
        return toWaitingListDTO(saved);
    }

    @Override
    public void removeFromWaitingList(Long waitingListId, User currentUser) {
        requireUser(currentUser);
        ListeAttente entry = listeAttenteRepository.findById(waitingListId)
                .orElseThrow(() -> new NotFoundException("Entree de liste d'attente introuvable"));
        ensureAdmin(currentUser);
        listeAttenteRepository.delete(entry);
        promoteFromWaitingListIfPossible(entry.getEvenement().getIdEvenement());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitingListDTO> getEventWaitingList(Long eventId, User currentUser) {
        requireUser(currentUser);
        findEvenement(eventId);
        return listeAttenteRepository.findByEvenementIdEvenementOrderByCreatedAtDesc(eventId)
                .stream()
                .map(this::toWaitingListDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void acceptWaitingListUser(Long waitingListId, User currentUser) {
        requireUser(currentUser);
        ListeAttente entry = listeAttenteRepository.findById(waitingListId)
                .orElseThrow(() -> new NotFoundException("Entree de liste d'attente introuvable"));
        ensureAdmin(currentUser);
        Evenement evenement = entry.getEvenement();
        User user = entry.getUser();

        if (!Boolean.TRUE.equals(evenement.getUnlimitedParticipants())
                && evenement.getCapacite() != null
                && safeParticipants(evenement) >= evenement.getCapacite()) {
            throw new BusinessRuleException("L'evenement est complet, veuillez d'abord liberer une place");
        }

        if (participationRepository.existsByUserIdAndEvenementIdEvenement(user.getId(), evenement.getIdEvenement())) {
            listeAttenteRepository.delete(entry);
            return;
        }

        Participation participation = Participation.builder()
                .user(user)
                .evenement(evenement)
                .build();
        evenement.setNombreParticipants(safeParticipants(evenement) + 1);
        participationRepository.save(participation);
        listeAttenteRepository.delete(entry);
    }

    @Override
    public void rejectWaitingListUser(Long waitingListId, User currentUser) {
        requireUser(currentUser);
        ListeAttente entry = listeAttenteRepository.findById(waitingListId)
                .orElseThrow(() -> new NotFoundException("Entree de liste d'attente introuvable"));
        ensureAdmin(currentUser);
        listeAttenteRepository.delete(entry);
        promoteFromWaitingListIfPossible(entry.getEvenement().getIdEvenement());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvenementResponseDTO> getArchivedEvenements(User currentUser) {
        return evenementRepository.findByStatusIgnoreCase(STATUS_COMPLETED, Pageable.unpaged())
                .stream()
                .map(event -> toDTO(event, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventSuggestionsDTO getEventSuggestions(User currentUser) {
        requireUser(currentUser);
        List<String> typeNames = evenementRepository.findActiveTypeNames();
        if (typeNames.isEmpty()) {
            return new EventSuggestionsDTO(List.of());
        }

        List<Object[]> userTypeRows = evenementRepository.countUsersByEventType();
        List<Object[]> typeEventRows = evenementRepository.findActiveEventsByType();

        List<CategorySuggestion> suggestions = typeNames.stream()
                .map(typeName -> {
                    long users = userTypeRows.stream()
                            .filter(row -> typeName.equalsIgnoreCase((String) row[0]))
                            .mapToLong(row -> (Long) row[1])
                            .findFirst()
                            .orElse(0L);
                    List<EventMatch> matches = typeEventRows.stream()
                            .filter(row -> typeName.equalsIgnoreCase((String) row[0]))
                            .map(row -> {
                                Evenement evt = (Evenement) row[1];
                                return new EventMatch(
                                        evt.getIdEvenement(),
                                        evt.getTitre(),
                                        resolveStatus(evt)
                                );
                            })
                            .collect(Collectors.toList());
                    return new CategorySuggestion(typeName, users, matches);
                })
                .sorted((a, b) -> Long.compare(b.getInterestedUsers(), a.getInterestedUsers()))
                .collect(Collectors.toList());

        return new EventSuggestionsDTO(suggestions);
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
        Long total = evenementRepository.count();
        return total;
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

    private WaitingListDTO toWaitingListDTO(ListeAttente entry) {
        User user = entry.getUser();
        Evenement evenement = entry.getEvenement();
        return WaitingListDTO.builder()
                .idListeAttente(entry.getIdListeAttente())
                .userId(user.getId())
                .userNom(user.getNom())
                .userEmail(user.getEmail())
                .evenementId(evenement.getIdEvenement())
                .evenementTitre(evenement.getTitre())
                .createdAt(entry.getCreatedAt())
                .build();
    }

    private ParticipationDTO toParticipationDTO(Participation participation, User currentUser) {
        return ParticipationDTO.builder()
                .idParticipation(participation.getIdParticipation())
                .userId(participation.getUser().getId())
                .userNom(participation.getUser().getNom())
                .userEmail(participation.getUser().getEmail())
                .evenementId(participation.getEvenement().getIdEvenement())
                .createdAt(participation.getCreatedAt())
                .status(participation.getStatus())
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

    private void ensureAdmin(User currentUser) {
        requireUser(currentUser);
        if (currentUser.getRole() != Role.ADMIN) {
            throw new BusinessRuleException("Seul un administrateur peut gerer les evenements");
        }
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

    private void promoteFromWaitingListIfPossible(Long eventId) {
        Evenement evenement = findEvenement(eventId);
        if (Boolean.TRUE.equals(evenement.getUnlimitedParticipants()) || evenement.getCapacite() == null) {
            return;
        }
        if (safeParticipants(evenement) >= evenement.getCapacite()) {
            return;
        }

        List<ListeAttente> waiting = listeAttenteRepository.findByEvenementIdEvenementOrderByCreatedAtDesc(eventId);
        for (ListeAttente entry : waiting) {
            if (safeParticipants(evenement) >= evenement.getCapacite()) {
                break;
            }
            if (participationRepository.existsByUserIdAndEvenementIdEvenement(entry.getUser().getId(), evenement.getIdEvenement())) {
                continue;
            }

            Participation participation = Participation.builder()
                    .user(entry.getUser())
                    .evenement(evenement)
                    .build();
            evenement.setNombreParticipants(safeParticipants(evenement) + 1);
            participationRepository.save(participation);
            listeAttenteRepository.delete(entry);
        }
    }
}
