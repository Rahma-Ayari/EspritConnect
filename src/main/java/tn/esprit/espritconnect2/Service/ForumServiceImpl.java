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
    public List<ForumPost> getFilteredPosts(Long categoryId, String authorRole, Boolean reported, String search) {
        Role role = null;
        if (authorRole != null && !authorRole.trim().isEmpty()) {
            try {
                role = Role.valueOf(authorRole.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Rôle invalide, on ignore le filtre
            }
        }
        String searchQuery = (search != null && !search.trim().isEmpty()) ? search : null;
        return postRepository.filterPosts(categoryId, role, reported, searchQuery);
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
}
