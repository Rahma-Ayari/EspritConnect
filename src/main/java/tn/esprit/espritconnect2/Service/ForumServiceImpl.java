package tn.esprit.espritconnect2.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.Repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements ForumService {

    private final ForumCategoryRepository categoryRepository;
    private final ForumPostRepository postRepository;
    private final ForumReplyRepository replyRepository;
    private final ForumGroupRepository groupRepository;
    private final ForumGroupMemberRepository groupMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ForumCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ForumCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable avec l'ID: " + id));
    }

    @Override
    @Transactional
    public ForumCategory createCategory(ForumCategory category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Une catégorie avec ce nom existe déjà.");
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public ForumCategory updateCategory(Long id, ForumCategory categoryDetails) {
        ForumCategory category = getCategoryById(id);
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setIcon(categoryDetails.getIcon());
        category.setColor(categoryDetails.getColor());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        ForumCategory category = getCategoryById(id);
        // Supprime tous les posts associés en cascade (géré au niveau DB ou service)
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getPostCountsPerCategory() {
        return categoryRepository.findAll().stream()
                .collect(Collectors.toMap(
                        ForumCategory::getId,
                        cat -> postRepository.countByCategoryId(cat.getId())
                ));
    }

    @Override
    @Transactional
    public List<ForumPost> getFilteredPosts(Long categoryId, String authorRole, Boolean reported, String search, Long groupId) {
        Role role = null;
        if (authorRole != null && !authorRole.trim().isEmpty()) {
            try {
                role = Role.valueOf(authorRole.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Rôle invalide, on ignore le filtre
            }
        }
        String searchQuery = (search != null && !search.trim().isEmpty()) ? search : null;
        return postRepository.filterPosts(categoryId, role, reported, searchQuery, groupId);
    }

    @Override
    @Transactional
    public ForumPost getPostById(Long id) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable avec l'ID: " + id));
        // Incrémente le compteur de vues
        post.setViewsCount(post.getViewsCount() + 1);
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public ForumPost createPost(ForumPost post) {
        // Validation de la catégorie
        if (post.getCategory() == null || post.getCategory().getId() == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        }
        ForumCategory category = getCategoryById(post.getCategory().getId());
        post.setCategory(category);
        post.setViewsCount(0);
        post.setPinned(false);
        post.setReported(false);

        if (post.getForumGroup() != null && post.getForumGroup().getId() != null) {
            ForumGroup group = groupRepository.findById(post.getForumGroup().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable."));
            if (group.getStatus() != GroupStatus.ACTIVE) {
                throw new IllegalArgumentException("Ce groupe n'est pas actif.");
            }
            // Vérifier si l'utilisateur est membre approuvé
            boolean isMember = groupMemberRepository.existsByGroupIdAndUserEmailAndStatus(
                    group.getId(), post.getAuthorEmail(), MemberStatus.APPROVED
            );
            if (!isMember) {
                throw new IllegalArgumentException("Vous devez être membre approuvé de ce groupe pour y publier.");
            }
            post.setForumGroup(group);
        }

        return postRepository.save(post);
    }

    @Override
    @Transactional
    public ForumPost updatePost(Long id, ForumPost postDetails) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        if (postDetails.getCategory() != null && postDetails.getCategory().getId() != null) {
            post.setCategory(getCategoryById(postDetails.getCategory().getId()));
        }
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public ForumPost togglePinPost(Long id) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        post.setPinned(!post.isPinned());
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public ForumPost reportPost(Long id, String reason) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        post.setReported(true);
        post.setReportReason(reason);
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public ForumPost resolvePostReport(Long id) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        post.setReported(false);
        post.setReportReason(null);
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public ForumReply addReply(Long postId, ForumReply reply) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        reply.setPost(post);
        reply.setReported(false);
        return replyRepository.save(reply);
    }

    @Override
    @Transactional
    public ForumReply updateReply(Long replyId, ForumReply replyDetails) {
        ForumReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable."));
        reply.setContent(replyDetails.getContent());
        return replyRepository.save(reply);
    }

    @Override
    @Transactional
    public void deleteReply(Long replyId) {
        ForumReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable."));
        replyRepository.delete(reply);
    }

    @Override
    @Transactional
    public ForumReply reportReply(Long replyId, String reason) {
        ForumReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable."));
        reply.setReported(true);
        reply.setReportReason(reason);
        return replyRepository.save(reply);
    }

    @Override
    @Transactional
    public ForumReply resolveReplyReport(Long replyId) {
        ForumReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable."));
        reply.setReported(false);
        reply.setReportReason(null);
        return replyRepository.save(reply);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumPost> getReportedPosts() {
        return postRepository.findByReportedTrueOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumReply> getReportedReplies() {
        return replyRepository.findByReportedTrueOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalPosts = postRepository.count();
        long totalReplies = replyRepository.count();
        long reportedPosts = postRepository.findByReportedTrueOrderByCreatedAtDesc().size();
        long reportedReplies = replyRepository.findByReportedTrueOrderByCreatedAtDesc().size();
        long totalReported = reportedPosts + reportedReplies;
        long totalCategories = categoryRepository.count();

        // Répartition par rôle
        Map<String, Long> roleDistribution = new HashMap<>();
        for (Role role : Role.values()) {
            long count = postRepository.countByAuthorRole(role) + replyRepository.countByAuthorRole(role);
            roleDistribution.put(role.name(), count);
        }

        stats.put("totalPosts", totalPosts);
        stats.put("totalReplies", totalReplies);
        stats.put("totalReported", totalReported);
        stats.put("reportedPostsCount", reportedPosts);
        stats.put("reportedRepliesCount", reportedReplies);
        stats.put("totalCategories", totalCategories);
        stats.put("roleDistribution", roleDistribution);

        return stats;
    }

    // --- SEEDING DES DONNÉES DE SIMULATION ---
    @PostConstruct
    @Transactional
    public void seedForumData() {
        // Seeding des Groupes et adhésions si inexistant
        if (groupRepository.count() == 0) {
            // 1. Groupes en attente de validation (Modération Admin)
            groupRepository.save(ForumGroup.builder()
                    .name("Club IA & Data Science Esprit")
                    .description("Communauté d'apprentissage et de partage de projets autour du Deep Learning, NLP et Computer Vision.")
                    .creatorEmail("etudiant.demo@esprit.tn")
                    .creatorName("Étudiant Demo")
                    .isPrivate(true)
                    .status(GroupStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            groupRepository.save(ForumGroup.builder()
                    .name("ESPRIT Alumni à l'International")
                    .description("Réseau d'entraide pour la recherche de postes et l'intégration des diplômés ESPRIT à l'étranger.")
                    .creatorEmail("alumni.demo@esprit.tn")
                    .creatorName("Alumni Demo")
                    .isPrivate(false)
                    .status(GroupStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            groupRepository.save(ForumGroup.builder()
                    .name("Esprit E-Sports & Gaming")
                    .description("Organisation des tournois internes et compétitions universitaires d'E-Sports.")
                    .creatorEmail("firas.ghorbel@esprit.tn")
                    .creatorName("Firas Ghorbel")
                    .isPrivate(false)
                    .status(GroupStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());

            // 2. Groupes actifs avec demandes de membres en attente (Modération Créateur/Owner)
            ForumGroup cyberGroup = groupRepository.save(ForumGroup.builder()
                    .name("Club Cybersécurité Esprit")
                    .description("Discussions, partages de Write-ups de CTF et ateliers pratiques de Pentesting.")
                    .creatorEmail("etudiant.demo@esprit.tn")
                    .creatorName("Étudiant Demo")
                    .isPrivate(true)
                    .status(GroupStatus.ACTIVE)
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .build());

            // Propriétaire approuvé
            groupMemberRepository.save(ForumGroupMember.builder()
                    .group(cyberGroup)
                    .userEmail("etudiant.demo@esprit.tn")
                    .userName("Étudiant Demo")
                    .role(GroupRole.OWNER)
                    .status(MemberStatus.APPROVED)
                    .joinedAt(LocalDateTime.now().minusDays(5))
                    .build());

            // Demandes de membres en attente
            groupMemberRepository.save(ForumGroupMember.builder()
                    .group(cyberGroup)
                    .userEmail("alumni.demo@esprit.tn")
                    .userName("Alumni Demo")
                    .role(GroupRole.MEMBER)
                    .status(MemberStatus.PENDING)
                    .joinedAt(LocalDateTime.now().minusDays(1))
                    .build());

            groupMemberRepository.save(ForumGroupMember.builder()
                    .group(cyberGroup)
                    .userEmail("yasmine.ayari@esprit.tn")
                    .userName("Yasmine Ayari")
                    .role(GroupRole.MEMBER)
                    .status(MemberStatus.PENDING)
                    .joinedAt(LocalDateTime.now().minusHours(4))
                    .build());

            groupMemberRepository.save(ForumGroupMember.builder()
                    .group(cyberGroup)
                    .userEmail("ahmed.mansour@esprit.tn")
                    .userName("Ahmed Mansour")
                    .role(GroupRole.MEMBER)
                    .status(MemberStatus.PENDING)
                    .joinedAt(LocalDateTime.now().minusHours(2))
                    .build());

            // Autre groupe actif pour tester l'autre rôle
            ForumGroup cloudGroup = groupRepository.save(ForumGroup.builder()
                    .name("DevOps & Cloud Computing")
                    .description("Communauté autour de AWS, Azure, Docker, Kubernetes et pipelines CI/CD.")
                    .creatorEmail("alumni.demo@esprit.tn")
                    .creatorName("Alumni Demo")
                    .isPrivate(true)
                    .status(GroupStatus.ACTIVE)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .build());

            groupMemberRepository.save(ForumGroupMember.builder()
                    .group(cloudGroup)
                    .userEmail("alumni.demo@esprit.tn")
                    .userName("Alumni Demo")
                    .role(GroupRole.OWNER)
                    .status(MemberStatus.APPROVED)
                    .joinedAt(LocalDateTime.now().minusDays(10))
                    .build());

            groupMemberRepository.save(ForumGroupMember.builder()
                    .group(cloudGroup)
                    .userEmail("etudiant.demo@esprit.tn")
                    .userName("Étudiant Demo")
                    .role(GroupRole.MEMBER)
                    .status(MemberStatus.PENDING)
                    .joinedAt(LocalDateTime.now().minusDays(2))
                    .build());
        }

        if (categoryRepository.count() > 0) {
            return; // Des données existent déjà, pas de seeding.
        }

        // 1. Création des catégories
        ForumCategory softwareCategory = categoryRepository.save(ForumCategory.builder()
                .name("Génie Logiciel & Programmation")
                .description("Discussions autour des technologies Web, Mobile, Algorithmique et architectures logicielles.")
                .icon("settings") // mappé à l'icône Material cog/settings
                .color("blue")
                .build());

        ForumCategory careerCategory = categoryRepository.save(ForumCategory.builder()
                .name("Orientation & Carrières")
                .description("Conseils professionnels, mentorat, opportunités de stages et insertion pour alumni et futurs diplômés.")
                .icon("briefcase") // mappé à l'icône Material briefcase
                .color("purple")
                .build());

        ForumCategory lifeCategory = categoryRepository.save(ForumCategory.builder()
                .name("Vie Estudiantine & Clubs")
                .description("Activités des clubs d'Esprit, événements, intégration, vie sur le campus et logement.")
                .icon("home") // mappé à l'icône Material home
                .color("green")
                .build());

        ForumCategory pfeCategory = categoryRepository.save(ForumCategory.builder()
                .name("Projets de Fin d'Études (PFE)")
                .description("Partage d'opportunités de PFE, conseils pour les rapports, soutenances et encadrement.")
                .icon("hand") // mappé à l'icône Material hand
                .color("amber")
                .build());

        // 2. Création de publications et commentaires réalistes
        // Post 1 - PFE & Stages (Épinglé)
        ForumPost post1 = postRepository.save(ForumPost.builder()
                .title("Conseils clés pour décrocher et réussir son stage PFE en Data / IA")
                .content("Bonjour à tous les Espritiens ! Anciennement étudiant à Esprit, j'ai décroché mon PFE dans une grande multinationale et j'y travaille aujourd'hui en tant que Data Scientist. Voici mes 3 conseils essentiels :\n\n1. Soyez irréprochables sur les fondamentaux (Python, SQL et notions de Machine Learning).\n2. Développez au moins un projet Github personnel complet (de l'ingestion à la modélisation) et mettez-le en valeur sur votre CV.\n3. Entraînez-vous à pitcher votre projet en 2 minutes.\n\nBon courage à tous !")
                .category(pfeCategory)
                .authorName("Karim Trabelsi")
                .authorEmail("karim.trabelsi.alumni@esprit.tn")
                .authorRole(Role.ALUMNI)
                .pinned(true)
                .reported(false)
                .viewsCount(240)
                .build());

        replyRepository.save(ForumReply.builder()
                .post(post1)
                .content("Merci beaucoup pour ces conseils précieux Karim ! Conseilles-tu de passer des certifications Cloud (AWS/Azure) pour se démarquer avant d'entamer le stage ?")
                .authorName("Yasmine Ayari")
                .authorEmail("yasmine.ayari@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .build());

        replyRepository.save(ForumReply.builder()
                .post(post1)
                .content("Bonjour Yasmine et Karim ! En tant qu'enseignant, je confirme à 100% les propos de Karim. Concernant ta question Yasmine : les certifications Cloud sont en effet un excellent signal, mais la pratique et la maîtrise d'un projet Git structuré restent prioritaires aux yeux des recruteurs techniques. Concentrez-vous sur le concret !")
                .authorName("Prof. Mohamed Ben Ali")
                .authorEmail("mohamed.benali@esprit.tn")
                .authorRole(Role.ENSEIGNANT)
                .build());

        // Post 2 - Vie Estudiantine & Clubs
        ForumPost post2 = postRepository.save(ForumPost.builder()
                .title("Quels sont les meilleurs clubs pour débuter en Cybersécurité à Esprit ?")
                .content("Salut tout le monde ! Je suis actuellement en 2ème année et je souhaite m'orienter vers la spécialité sécurité. J'aimerais savoir quels clubs ou communautés au sein d'Esprit organisent le plus d'ateliers pratiques et de CTF pour les grands débutants. Merci d'avance !")
                .category(lifeCategory)
                .authorName("Skander Feki")
                .authorEmail("skander.feki@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .pinned(false)
                .reported(false)
                .viewsCount(85)
                .build());

        replyRepository.save(ForumReply.builder()
                .post(post2)
                .content("Salut Skander ! Tu devrais absolument faire un tour chez EspritSec ou Esprit Hack. Ils animent des formations hebdomadaires de vulgarisation et préparent les membres aux compétitions nationales de CTF. L'ambiance y est super collaborative !")
                .authorName("Salma Rebai")
                .authorEmail("salma.rebai@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .build());

        // Post 3 - Génie Logiciel
        ForumPost post3 = postRepository.save(ForumPost.builder()
                .title("Supports de cours additionnels - Architecture Microservices & Angular")
                .content("Chers étudiants de 4ème année, j'ai mis à votre disposition sur notre espace partagé des exemples complets d'implémentation de passerelles API (Spring Cloud Gateway) ainsi que l'interconnexion avec un frontend Angular. Ces ressources complètent notre séance de travaux pratiques de cette semaine.")
                .category(softwareCategory)
                .authorName("Prof. Leila Jouini")
                .authorEmail("leila.jouini@esprit.tn")
                .authorRole(Role.ENSEIGNANT)
                .pinned(false)
                .reported(false)
                .viewsCount(150)
                .build());

        replyRepository.save(ForumReply.builder()
                .post(post3)
                .content("Un grand merci Madame ! Les templates nous font gagner un temps précieux pour notre projet intégré.")
                .authorName("Ahmed Mansour")
                .authorEmail("ahmed.mansour@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .build());

        // Post 4 - Post Signalé pour tester la modération
        ForumPost post4 = postRepository.save(ForumPost.builder()
                .title("Vente de projets intégrés tout faits - 100% garantis")
                .content("Hey les gars ! Si vous avez la flemme de coder votre projet de génie logiciel ou de Web, je propose des projets Angular/Spring complets prêts à l'envoi avec rapports rédigés. Contactez-moi par message privé sur Telegram @HackEsprit. Prix très attractif !")
                .category(careerCategory)
                .authorName("Utilisateur Suspect")
                .authorEmail("suspect@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .pinned(false)
                .reported(true)
                .reportReason("Vente illégale de projets universitaires - Plagiat académique")
                .viewsCount(12)
                .build());

        replyRepository.save(ForumReply.builder()
                .post(post4)
                .content("C'est totalement interdit et passible d'exclusion définitive du conseil de discipline ! Merci de supprimer ce message.")
                .authorName("Firas Ghorbel")
                .authorEmail("firas.ghorbel@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .build());

        // Commentaire signalé sur le post 1 pour démo
        ForumReply replySignale = replyRepository.save(ForumReply.builder()
                .post(post1)
                .content("Message publicitaire spam : Visitez mon site web frauduleux pour acheter des Bitcoins faciles !")
                .authorName("Spammer Anonyme")
                .authorEmail("spam@spambot.com")
                .authorRole(Role.ALUMNI)
                .reported(true)
                .reportReason("Publicité / Spam commercial indésirable")
                .build());

    }

    // ==========================================
    //            GROUPS IMPLEMENTATION
    // ==========================================

    @Override
    @Transactional
    public ForumGroup createGroup(ForumGroup group) {
        if (group.getName() == null || group.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du groupe est obligatoire.");
        }
        if (groupRepository.findAll().stream().anyMatch(g -> g.getName().equalsIgnoreCase(group.getName().trim()))) {
            throw new IllegalArgumentException("Un groupe avec ce nom existe déjà.");
        }
        group.setStatus(GroupStatus.PENDING);
        ForumGroup savedGroup = groupRepository.save(group);

        // Rejoindre automatiquement le créateur en tant que OWNER et APPROVED
        ForumGroupMember creatorMember = ForumGroupMember.builder()
                .group(savedGroup)
                .userEmail(group.getCreatorEmail())
                .userName(group.getCreatorName())
                .role(GroupRole.OWNER)
                .status(MemberStatus.APPROVED)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(creatorMember);

        return savedGroup;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumGroup> getActiveGroups() {
        return groupRepository.findByStatus(GroupStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumGroup> getPendingGroups() {
        return groupRepository.findByStatus(GroupStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumGroup> getMyGroups(String userEmail) {
        return groupMemberRepository.findByUserEmailAndStatus(userEmail, MemberStatus.APPROVED)
                .stream()
                .map(ForumGroupMember::getGroup)
                .filter(g -> g.getStatus() == GroupStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ForumGroup approveGroup(Long groupId) {
        ForumGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable."));
        if (group.getStatus() != GroupStatus.PENDING) {
            throw new IllegalArgumentException("Le groupe n'est pas en attente d'approbation.");
        }
        group.setStatus(GroupStatus.ACTIVE);
        return groupRepository.save(group);
    }

    @Override
    @Transactional
    public ForumGroup rejectGroup(Long groupId) {
        ForumGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable."));
        if (group.getStatus() != GroupStatus.PENDING) {
            throw new IllegalArgumentException("Le groupe n'est pas en attente d'approbation.");
        }
        group.setStatus(GroupStatus.REJECTED);
        return groupRepository.save(group);
    }

    @Override
    @Transactional
    public ForumGroupMember requestJoinGroup(Long groupId, String userEmail, String userName) {
        ForumGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable."));
        if (group.getStatus() != GroupStatus.ACTIVE) {
            throw new IllegalArgumentException("Ce groupe n'est pas actif.");
        }

        Optional<ForumGroupMember> existingOpt = groupMemberRepository.findByGroupIdAndUserEmail(groupId, userEmail);
        if (existingOpt.isPresent()) {
            ForumGroupMember existing = existingOpt.get();
            if (existing.getStatus() == MemberStatus.APPROVED) {
                throw new IllegalArgumentException("Vous êtes déjà membre de ce groupe.");
            } else if (existing.getStatus() == MemberStatus.PENDING) {
                throw new IllegalArgumentException("Votre demande d'adhésion est déjà en attente.");
            } else {
                existing.setStatus(group.isPrivate() ? MemberStatus.PENDING : MemberStatus.APPROVED);
                existing.setJoinedAt(LocalDateTime.now());
                return groupMemberRepository.save(existing);
            }
        }

        MemberStatus initialStatus = group.isPrivate() ? MemberStatus.PENDING : MemberStatus.APPROVED;
        ForumGroupMember member = ForumGroupMember.builder()
                .group(group)
                .userEmail(userEmail)
                .userName(userName)
                .role(GroupRole.MEMBER)
                .status(initialStatus)
                .joinedAt(LocalDateTime.now())
                .build();

        return groupMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumGroupMember> getPendingMemberships(Long groupId, String currentUserEmail) {
        ForumGroupMember requester = groupMemberRepository.findByGroupIdAndUserEmail(groupId, currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Accès refusé. Vous n'êtes pas membre de ce groupe."));
        if (requester.getRole() != GroupRole.OWNER && requester.getRole() != GroupRole.MODERATOR) {
            throw new IllegalArgumentException("Accès refusé. Vous devez être modérateur ou propriétaire.");
        }
        return groupMemberRepository.findByGroupIdAndStatus(groupId, MemberStatus.PENDING);
    }

    @Override
    @Transactional
    public ForumGroupMember approveMembership(Long groupId, Long memberId, String currentUserEmail) {
        ForumGroupMember requester = groupMemberRepository.findByGroupIdAndUserEmail(groupId, currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Accès refusé."));
        if (requester.getRole() != GroupRole.OWNER && requester.getRole() != GroupRole.MODERATOR) {
            throw new IllegalArgumentException("Accès refusé. Permission insuffisante.");
        }

        ForumGroupMember member = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'adhésion introuvable."));
        if (!member.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Le membre ne correspond pas au groupe spécifié.");
        }
        member.setStatus(MemberStatus.APPROVED);
        member.setJoinedAt(LocalDateTime.now());
        return groupMemberRepository.save(member);
    }

    @Override
    @Transactional
    public ForumGroupMember rejectMembership(Long groupId, Long memberId, String currentUserEmail) {
        ForumGroupMember requester = groupMemberRepository.findByGroupIdAndUserEmail(groupId, currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Accès refusé."));
        if (requester.getRole() != GroupRole.OWNER && requester.getRole() != GroupRole.MODERATOR) {
            throw new IllegalArgumentException("Accès refusé. Permission insuffisante.");
        }

        ForumGroupMember member = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'adhésion introuvable."));
        if (!member.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Le membre ne correspond pas au groupe spécifié.");
        }
        member.setStatus(MemberStatus.REJECTED);
        return groupMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void leaveGroup(Long groupId, String userEmail) {
        ForumGroupMember member = groupMemberRepository.findByGroupIdAndUserEmail(groupId, userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Vous n'êtes pas membre de ce groupe."));
        if (member.getRole() == GroupRole.OWNER) {
            throw new IllegalArgumentException("Le propriétaire ne peut pas quitter le groupe sans d'abord transférer la propriété.");
        }
        groupMemberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumGroupMember> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupIdAndStatus(groupId, MemberStatus.APPROVED);
    }

    @Override
    @Transactional(readOnly = true)
    public ForumGroup getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable avec l'ID: " + groupId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumGroupMember> getUserMemberships(String userEmail) {
        return groupMemberRepository.findByUserEmail(userEmail);
    }
}
