package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.AdminDashboardResponseDTO;
import tn.esprit.espritconnect2.Entitie.Alumni;
import tn.esprit.espritconnect2.Entitie.Entreprise;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final EnumSet<Role> PENDING_USER_ROLES = EnumSet.of(Role.ETUDIANT, Role.ALUMNI);

    private final EntrepriseRepository entrepriseRepository;
    private final EtudiantRepository etudiantRepository;
    private final AlumniRepository alumniRepository;
    private final OffreRepository offreRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public AdminDashboardResponseDTO getDashboardData() {
        long students = etudiantRepository.countApprovedEtudiants();
        long alumni = alumniRepository.countApprovedAlumni();
        long companies = entrepriseRepository.countApprovedEntreprises();
        long totalUsers = students + alumni + companies;

        long pendingCompanies = entrepriseRepository.countPendingEntreprises();
        long pendingUsers = userRepository.countByEnabledFalseAndInscriptionRefuseeFalseAndRoleIn(PENDING_USER_ROLES);
        long pendingApprovalsCount = pendingCompanies + pendingUsers;

        List<AdminDashboardResponseDTO.PendingApprovalItem> pendingApprovals = getPendingApprovals(3);

        LocalDate now = LocalDate.now();
        LocalDate startThisMonth = now.withDayOfMonth(1);
        LocalDate startNextMonth = startThisMonth.plusMonths(1);
        LocalDate startLastMonth = startThisMonth.minusMonths(1);

        Date thisMonthStartDate = toDate(startThisMonth);
        Date nextMonthStartDate = toDate(startNextMonth);
        Date lastMonthStartDate = toDate(startLastMonth);

        LocalDateTime thisMonthStartLdt = startThisMonth.atStartOfDay();
        LocalDateTime nextMonthStartLdt = startNextMonth.atStartOfDay();
        LocalDateTime lastMonthStartLdt = startLastMonth.atStartOfDay();

        long thisMonthRegistrations = etudiantRepository.countApprovedByDateInscriptionBetween(thisMonthStartDate, nextMonthStartDate)
                + userRepository.countApprovedByRoleAndCreatedAtBetween(Role.ALUMNI, thisMonthStartLdt, nextMonthStartLdt);
        long lastMonthRegistrations = etudiantRepository.countApprovedByDateInscriptionBetween(lastMonthStartDate, thisMonthStartDate)
                + userRepository.countApprovedByRoleAndCreatedAtBetween(Role.ALUMNI, lastMonthStartLdt, thisMonthStartLdt);
        long registrationDelta = computeDeltaPercent(thisMonthRegistrations, lastMonthRegistrations);

        long activeJobPostings = offreRepository.findAll()
                .stream()
                .filter(offre -> offre.getStatutOfrre() != Status.REFUSEE)
                .count();
        long thisMonthJobPostings = offreRepository.countByDatePublicationBetween(thisMonthStartDate, nextMonthStartDate);
        long lastMonthJobPostings = offreRepository.countByDatePublicationBetween(lastMonthStartDate, thisMonthStartDate);
        long jobPostingDelta = computeDeltaPercent(thisMonthJobPostings, lastMonthJobPostings);

        long sentEmailCampaigns = notificationRepository.countByDateEnvoiBetween(thisMonthStartDate, nextMonthStartDate);
        long lastMonthEmailCampaigns = notificationRepository.countByDateEnvoiBetween(lastMonthStartDate, thisMonthStartDate);
        long emailCampaignDelta = computeDeltaPercent(sentEmailCampaigns, lastMonthEmailCampaigns);
        long openedEmails = notificationRepository.countByLueTrueAndDateEnvoiBetween(thisMonthStartDate, nextMonthStartDate);
        long avgOpenRate = sentEmailCampaigns == 0 ? 0 : Math.round((openedEmails * 100.0) / sentEmailCampaigns);

        return AdminDashboardResponseDTO.builder()
                .summary(AdminDashboardResponseDTO.Summary.builder()
                        .totalUsers(totalUsers)
                        .students(students)
                        .alumni(alumni)
                        .companies(companies)
                        .build())
                .pendingApprovalsCount(pendingApprovalsCount)
                .pendingApprovals(pendingApprovals)
                .activityOverview(AdminDashboardResponseDTO.ActivityOverview.builder()
                        .newRegistrations(thisMonthRegistrations)
                        .registrationDeltaPercent(registrationDelta)
                        .activeJobPostings(activeJobPostings)
                        .jobPostingDeltaPercent(jobPostingDelta)
                        .sentEmailCampaigns(sentEmailCampaigns)
                        .emailCampaignDeltaPercent(emailCampaignDelta)
                        .avgOpenRatePercent(avgOpenRate)
                        .build())
                .quickStart(AdminDashboardResponseDTO.QuickStart.builder()
                        .completedSteps(3)
                        .totalSteps(5)
                        .build())
                .build();
    }

    public List<AdminDashboardResponseDTO.PendingApprovalItem> getPendingApprovals(int limit) {
        List<PendingRow> rows = new ArrayList<>();

        List<Entreprise> pendingCompanies = entrepriseRepository.findPendingEntreprises();
        for (Entreprise e : pendingCompanies) {
            long sort = e.getCreatedAt() != null
                    ? e.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : e.getIdEntreprise();
            rows.add(new PendingRow(sort, toPendingCompanyItem(e)));
        }

        List<User> pendingUsers = userRepository.findByEnabledFalseAndInscriptionRefuseeFalseAndRoleInOrderByCreatedAtDesc(PENDING_USER_ROLES);
        for (User u : pendingUsers) {
            long sort = u.getCreatedAt() != null
                    ? u.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : 0L;
            rows.add(new PendingRow(sort, toPendingUserItem(u)));
        }

        return rows.stream()
                .sorted(Comparator.comparingLong(PendingRow::sortMillis).reversed())
                .map(PendingRow::item)
                .limit(Math.max(limit, 0))
                .toList();
    }

    @Transactional
    public void approveCompany(Long companyId) {
        Entreprise entreprise = entrepriseRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));
        entreprise.setValide(true);
        entreprise.setVerificationStatus(tn.esprit.espritconnect2.Entitie.VerificationStatus.VERIFIED);
        entreprise.setInscriptionRefusee(false);
        entrepriseRepository.save(entreprise);
    }

    @Transactional
    public void declineCompany(Long companyId) {
        Entreprise entreprise = entrepriseRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));
        entreprise.setValide(false);
        entreprise.setInscriptionRefusee(true);
        entrepriseRepository.save(entreprise);
    }

    @Transactional
    public void approveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if (!PENDING_USER_ROLES.contains(user.getRole())) {
            throw new RuntimeException("Ce compte ne peut pas être approuvé depuis cette file.");
        }
        user.setEnabled(true);
        user.setInscriptionRefusee(false);
        userRepository.save(user);
    }

    @Transactional
    public void declineUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if (!PENDING_USER_ROLES.contains(user.getRole())) {
            throw new RuntimeException("Ce compte ne peut pas être refusé depuis cette file.");
        }
        user.setEnabled(false);
        user.setInscriptionRefusee(true);
        userRepository.save(user);
    }

    private AdminDashboardResponseDTO.PendingApprovalItem toPendingCompanyItem(Entreprise entreprise) {
        return AdminDashboardResponseDTO.PendingApprovalItem.builder()
                .profileType("COMPANY")
                .companyId(entreprise.getIdEntreprise())
                .userId(null)
                .companyName(entreprise.getNom())
                .sector(entreprise.getSecteur() == null ? "Entreprise" : entreprise.getSecteur())
                .statusLabel("En attente de validation")
                .build();
    }

    private AdminDashboardResponseDTO.PendingApprovalItem toPendingUserItem(User user) {
        String detail;
        if (user.getRole() == Role.ETUDIANT) {
            detail = etudiantRepository.findByEmail(user.getEmail())
                    .map(this::formatEtudiantDetail)
                    .orElse("Étudiant");
            return AdminDashboardResponseDTO.PendingApprovalItem.builder()
                    .profileType("ETUDIANT")
                    .companyId(null)
                    .userId(user.getId().toString())
                    .companyName(user.getNom())
                    .sector(detail)
                    .statusLabel("Compte en attente")
                    .build();
        }
        if (user.getRole() == Role.ALUMNI) {
            detail = alumniRepository.findByEmail(user.getEmail())
                    .map(this::formatAlumniDetail)
                    .orElse("Alumni");
            return AdminDashboardResponseDTO.PendingApprovalItem.builder()
                    .profileType("ALUMNI")
                    .companyId(null)
                    .userId(user.getId().toString())
                    .companyName(user.getNom())
                    .sector(detail)
                    .statusLabel("Compte en attente")
                    .build();
        }
        throw new IllegalStateException("Rôle inattendu dans la file d'attente");
    }

    private String formatEtudiantDetail(Etudiant e) {
        String filiere = e.getFiliere() != null ? e.getFiliere() : "";
        String niveau = e.getNiveau() != null ? e.getNiveau().name() : "";
        String joined = (filiere + " · " + niveau).trim();
        if (joined.startsWith("·")) joined = joined.substring(1).trim();
        if (joined.endsWith("·")) joined = joined.substring(0, joined.length() - 1).trim();
        return joined.isEmpty() ? "Étudiant" : joined;
    }

    private String formatAlumniDetail(Alumni a) {
        String domaine = a.getDomaine() != null ? a.getDomaine() : "";
        String promo = a.getAnneePromotion() != null ? "Promo " + a.getAnneePromotion() : "";
        String joined = (domaine + " · " + promo).trim();
        if (joined.startsWith("·")) joined = joined.substring(1).trim();
        if (joined.endsWith("·")) joined = joined.substring(0, joined.length() - 1).trim();
        return joined.isEmpty() ? "Alumni" : joined;
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private long computeDeltaPercent(long current, long previous) {
        if (previous <= 0) {
            return current > 0 ? 100 : 0;
        }
        return Math.round(((double) (current - previous) / previous) * 100);
    }

    private record PendingRow(long sortMillis, AdminDashboardResponseDTO.PendingApprovalItem item) {}
}
