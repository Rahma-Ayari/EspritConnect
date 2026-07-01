package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.ProfilRequestDTO;
import tn.esprit.espritconnect2.DTO.ProfilResponseDTO;
import tn.esprit.espritconnect2.Entitie.Profil;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Alumni;
import tn.esprit.espritconnect2.Repository.ProfilRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.AlumniRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation du service Profil.
 *
 * @Service   → Spring gère cette classe comme un bean de la couche service.
 * @RequiredArgsConstructor → injection du repository via constructeur (pas @Autowired).
 */
@Service
@RequiredArgsConstructor
public class ProfilServiceImpl implements IProfilService {

    private final ProfilRepository profilRepository;
    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final AlumniRepository alumniRepository;
    private final AdminNotificationEventService adminNotificationEventService;

    // ─── Mapper DTO → Entité ──────────────────────────────────────────────────
    /**
     * Convertit un ProfilRequestDTO en entité Profil.
     * On ne touche pas aux relations (etudiant, alumni...) ici :
     * elles sont gérées côté Etudiant/Alumni lors de leur propre création.
     */
    private Profil toEntity(ProfilRequestDTO dto) {
        Profil profil = new Profil();
        profil.setUserId(dto.getUserId());
        profil.setPhoto(dto.getPhoto());
        profil.setLienLinkedIn(dto.getLienLinkedIn());
        profil.setBio(dto.getBio());
        profil.setLienGitHub(dto.getLienGitHub());
        profil.setPrenom(dto.getPrenom());
        profil.setTelephone(dto.getTelephone());
        profil.setAdresse(dto.getAdresse());
        profil.setVille(dto.getVille());
        profil.setPays(dto.getPays());
        profil.setCodePostal(dto.getCodePostal());
        profil.setSiteWeb(dto.getSiteWeb());
        profil.setDateNaissance(dto.getDateNaissance());
        profil.setGenre(dto.getGenre());
        profil.setNomProprietaire(dto.getNomProprietaire());
        return profil;
    }

    // ─── Mapper Entité → ResponseDTO ─────────────────────────────────────────
    /**
     * Convertit une entité Profil en ProfilResponseDTO.
     * On détecte automatiquement le type et le nom du propriétaire
     * en vérifiant quelle relation est non-nulle.
     */
    private ProfilResponseDTO toDTO(Profil profil) {

        // Déterminer dynamiquement le propriétaire du profil
        String nomProprietaire = profil.getNomProprietaire(); // Priorité au nom stocké dans le profil
        String typeProprietaire = "INCONNU";
        String prenom = profil.getPrenom();

        String niveau = null;
        String filiere = null;
        Integer anneePromotion = null;
        String domaine = null;
        Boolean disponibleMentorat = null;
        String entrepriseActuelle = null;
        String registreCommerce = null;
        String secteurActivite = null;
        String descriptionEntreprise = null;

        // Chercher l'utilisateur par email (userId) pour récupérer ses informations d'inscription
        Optional<User> userOpt = userRepository.findByEmail(profil.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Séparer nom et prénom à partir du nom complet d'inscription si non renseigné dans le profil
            String fullName = user.getNom();
            if (fullName != null && fullName.contains(" ")) {
                int lastSpaceIndex = fullName.lastIndexOf(" ");
                String parsedPrenom = fullName.substring(0, lastSpaceIndex).trim();
                String parsedNom = fullName.substring(lastSpaceIndex + 1).trim();
                if (prenom == null || prenom.isEmpty()) {
                    prenom = parsedPrenom;
                }
                if (nomProprietaire == null || nomProprietaire.isEmpty() || "Inconnu".equals(nomProprietaire) || "Utilisateur".equals(nomProprietaire)) {
                    nomProprietaire = parsedNom;
                }
            } else {
                if (nomProprietaire == null || nomProprietaire.isEmpty() || "Inconnu".equals(nomProprietaire) || "Utilisateur".equals(nomProprietaire)) {
                    nomProprietaire = fullName;
                }
            }
            
            typeProprietaire = user.getRole().name();
            
            if (user.getRole() == tn.esprit.espritconnect2.Entitie.Role.ETUDIANT) {
                Optional<Etudiant> etudiantOpt = etudiantRepository.findByEmail(user.getEmail());
                if (etudiantOpt.isPresent()) {
                    Etudiant etudiant = etudiantOpt.get();
                    niveau = etudiant.getNiveau() != null ? etudiant.getNiveau().name() : null;
                    filiere = etudiant.getFiliere();
                }
            } else if (user.getRole() == tn.esprit.espritconnect2.Entitie.Role.ALUMNI) {
                Optional<Alumni> alumniOpt = alumniRepository.findByEmail(user.getEmail());
                if (alumniOpt.isPresent()) {
                    Alumni alumni = alumniOpt.get();
                    anneePromotion = alumni.getAnneePromotion();
                    domaine = alumni.getDomaine();
                    disponibleMentorat = alumni.getDisponibleMentorat();
                    entrepriseActuelle = alumni.getEntrepriseActuelle();
                }
            } else if (user.getRole() == tn.esprit.espritconnect2.Entitie.Role.ENTREPRISE) {
                registreCommerce = user.getBusinessRegistrationNumber();
                secteurActivite = user.getCompanySector();
                descriptionEntreprise = user.getCompanyDescription();
            }
        }

        // Fallbacks pour relations si direct mapping a échoué
        if (profil.getEtudiant() != null) {
            typeProprietaire = "ETUDIANT";
            if (nomProprietaire == null || nomProprietaire.isEmpty() || "Inconnu".equals(nomProprietaire)) {
                nomProprietaire = profil.getEtudiant().getNom();
            }
            niveau = profil.getEtudiant().getNiveau() != null ? profil.getEtudiant().getNiveau().name() : null;
            filiere = profil.getEtudiant().getFiliere();
        } else if (profil.getAlumni() != null) {
            typeProprietaire = "ALUMNI";
            if (nomProprietaire == null || nomProprietaire.isEmpty() || "Inconnu".equals(nomProprietaire)) {
                nomProprietaire = profil.getAlumni().getNom();
            }
            anneePromotion = profil.getAlumni().getAnneePromotion();
            domaine = profil.getAlumni().getDomaine();
            disponibleMentorat = profil.getAlumni().getDisponibleMentorat();
            entrepriseActuelle = profil.getAlumni().getEntrepriseActuelle();
        } else if (profil.getEntreprise() != null) {
            typeProprietaire = "ENTREPRISE";
            if (nomProprietaire == null || nomProprietaire.isEmpty() || "Inconnu".equals(nomProprietaire)) {
                nomProprietaire = profil.getEntreprise().getNom();
            }
        }

        if (nomProprietaire == null || nomProprietaire.isEmpty()) {
            nomProprietaire = "Inconnu";
        }

        return ProfilResponseDTO.builder()
                .idProfil(profil.getIdProfil())
                .userId(profil.getUserId())
                .photo(profil.getPhoto())
                .lienLinkedIn(profil.getLienLinkedIn())
                .bio(profil.getBio())
                .lienGitHub(profil.getLienGitHub())
                .prenom(prenom)
                .telephone(profil.getTelephone())
                .adresse(profil.getAdresse())
                .ville(profil.getVille())
                .pays(profil.getPays())
                .codePostal(profil.getCodePostal())
                .siteWeb(profil.getSiteWeb())
                .dateNaissance(profil.getDateNaissance())
                .genre(profil.getGenre())
                .nomProprietaire(nomProprietaire)
                .typeProprietaire(typeProprietaire)
                .niveau(niveau)
                .filiere(filiere)
                .anneePromotion(anneePromotion)
                .domaine(domaine)
                .disponibleMentorat(disponibleMentorat)
                .entrepriseActuelle(entrepriseActuelle)
                .registreCommerce(registreCommerce)
                .secteurActivite(secteurActivite)
                .descriptionEntreprise(descriptionEntreprise)
                .build();
    }

    private void updateRegistrationFields(String email, ProfilRequestDTO dto) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Sync name if Prenom and Nom are provided
            if (dto.getPrenom() != null && dto.getNomProprietaire() != null) {
                user.setNom(dto.getPrenom().trim() + " " + dto.getNomProprietaire().trim());
            }
            
            if (user.getRole() == tn.esprit.espritconnect2.Entitie.Role.ETUDIANT) {
                Optional<Etudiant> etudiantOpt = etudiantRepository.findByEmail(email);
                if (etudiantOpt.isPresent()) {
                    Etudiant etudiant = etudiantOpt.get();
                    if (dto.getPrenom() != null && dto.getNomProprietaire() != null) {
                        etudiant.setNom(dto.getPrenom().trim() + " " + dto.getNomProprietaire().trim());
                    }
                    if (dto.getNiveau() != null && !dto.getNiveau().isEmpty()) {
                        try {
                            etudiant.setNiveau(tn.esprit.espritconnect2.Entitie.Niveau.valueOf(dto.getNiveau()));
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                    if (dto.getFiliere() != null) {
                        etudiant.setFiliere(dto.getFiliere());
                    }
                    etudiantRepository.save(etudiant);
                }
            } else if (user.getRole() == tn.esprit.espritconnect2.Entitie.Role.ALUMNI) {
                Optional<Alumni> alumniOpt = alumniRepository.findByEmail(email);
                if (alumniOpt.isPresent()) {
                    Alumni alumni = alumniOpt.get();
                    if (dto.getPrenom() != null && dto.getNomProprietaire() != null) {
                        alumni.setNom(dto.getPrenom().trim() + " " + dto.getNomProprietaire().trim());
                    }
                    if (dto.getAnneePromotion() != null) {
                        alumni.setAnneePromotion(dto.getAnneePromotion());
                    }
                    if (dto.getDomaine() != null) {
                        alumni.setDomaine(dto.getDomaine());
                    }
                    if (dto.getDisponibleMentorat() != null) {
                        alumni.setDisponibleMentorat(dto.getDisponibleMentorat());
                    }
                    if (dto.getEntrepriseActuelle() != null) {
                        alumni.setEntrepriseActuelle(dto.getEntrepriseActuelle());
                    }
                    alumniRepository.save(alumni);
                }
            } else if (user.getRole() == tn.esprit.espritconnect2.Entitie.Role.ENTREPRISE) {
                if (dto.getRegistreCommerce() != null) {
                    user.setBusinessRegistrationNumber(dto.getRegistreCommerce());
                }
                if (dto.getSecteurActivite() != null) {
                    user.setCompanySector(dto.getSecteurActivite());
                }
                if (dto.getDescriptionEntreprise() != null) {
                    user.setCompanyDescription(dto.getDescriptionEntreprise());
                }
            }
            userRepository.save(user);
        }
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────
    @Override
    public ProfilResponseDTO creerProfil(ProfilRequestDTO dto) {
        // Vérifier que le userId n'est pas déjà utilisé
        if (dto.getUserId() != null && profilRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("Un profil avec ce userId existe déjà : " + dto.getUserId());
        }

        updateRegistrationFields(dto.getUserId(), dto);

        Profil profil = toEntity(dto);
        Profil saved = profilRepository.save(profil);
        userRepository.findByEmail(saved.getUserId()).ifPresent(adminNotificationEventService::notifyProfileUpdated);
        return toDTO(saved);
    }

    // ─── READ ALL ─────────────────────────────────────────────────────────────
    @Override
    public List<ProfilResponseDTO> getAllProfils() {
        return profilRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── READ BY ID ───────────────────────────────────────────────────────────
    @Override
    public ProfilResponseDTO getProfilById(Long id) {
        Profil profil = profilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profil introuvable avec l'id : " + id));
        return toDTO(profil);
    }

    // ─── READ BY USER ID ──────────────────────────────────────────────────────
    @Override
    public ProfilResponseDTO getProfilByUserId(String userId) {
        Profil profil = profilRepository.findByUserId(userId)
                .orElse(null);
        if (profil == null) {
            // Si le profil n'existe pas encore, on génère un profil par défaut 
            // basé sur les informations d'inscription de l'utilisateur (fallback)
            return getCurrentUserProfile(userId);
        }
        return toDTO(profil);
    }

    // ─── READ CURRENT USER PROFILE ─────────────────────────────────────────────
    @Override
    public ProfilResponseDTO getCurrentUserProfile(String email) {
        Profil profil = profilRepository.findByUserId(email)
                .orElse(null);
        if (profil == null) {
            String nom = "Utilisateur";
            String prenom = "";
            String role = "INCONNU";
            
            String niveau = null;
            String filiere = null;
            Integer anneePromotion = null;
            String domaine = null;
            Boolean disponibleMentorat = null;
            String entrepriseActuelle = null;
            String registreCommerce = null;
            String secteurActivite = null;
            String descriptionEntreprise = null;
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String fullName = user.getNom();
                if (fullName != null && fullName.contains(" ")) {
                    int lastSpaceIndex = fullName.lastIndexOf(" ");
                    prenom = fullName.substring(0, lastSpaceIndex).trim();
                    nom = fullName.substring(lastSpaceIndex + 1).trim();
                } else {
                    nom = fullName;
                }
                role = user.getRole().name();
                
                if (user.getRole() == tn.esprit.espritconnect2.Entitie.Role.ETUDIANT) {
                    Optional<Etudiant> etudiantOpt = etudiantRepository.findByEmail(user.getEmail());
                    if (etudiantOpt.isPresent()) {
                        Etudiant etudiant = etudiantOpt.get();
                        niveau = etudiant.getNiveau() != null ? etudiant.getNiveau().name() : null;
                        filiere = etudiant.getFiliere();
                    }
                } else if (user.getRole() == tn.esprit.espritconnect2.Entitie.Role.ALUMNI) {
                    Optional<Alumni> alumniOpt = alumniRepository.findByEmail(user.getEmail());
                    if (alumniOpt.isPresent()) {
                        Alumni alumni = alumniOpt.get();
                        anneePromotion = alumni.getAnneePromotion();
                        domaine = alumni.getDomaine();
                        disponibleMentorat = alumni.getDisponibleMentorat();
                        entrepriseActuelle = alumni.getEntrepriseActuelle();
                    }
                } else if (user.getRole() == tn.esprit.espritconnect2.Entitie.Role.ENTREPRISE) {
                    registreCommerce = user.getBusinessRegistrationNumber();
                    secteurActivite = user.getCompanySector();
                    descriptionEntreprise = user.getCompanyDescription();
                }
            }
            return ProfilResponseDTO.builder()
                    .userId(email)
                    .nomProprietaire(nom)
                    .prenom(prenom)
                    .typeProprietaire(role)
                    .niveau(niveau)
                    .filiere(filiere)
                    .anneePromotion(anneePromotion)
                    .domaine(domaine)
                    .disponibleMentorat(disponibleMentorat)
                    .entrepriseActuelle(entrepriseActuelle)
                    .registreCommerce(registreCommerce)
                    .secteurActivite(secteurActivite)
                    .descriptionEntreprise(descriptionEntreprise)
                    .build();
        }
        return toDTO(profil);
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────
    @Override
    public ProfilResponseDTO updateProfil(Long id, ProfilRequestDTO dto) {
        Profil profil = profilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profil introuvable avec l'id : " + id));

        // Vérifier unicité du userId seulement s'il a changé
        if (dto.getUserId() != null
                && !dto.getUserId().equals(profil.getUserId())
                && profilRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("Ce userId est déjà utilisé : " + dto.getUserId());
        }

        updateRegistrationFields(dto.getUserId(), dto);

        // Mise à jour uniquement des champs simples
        // Les relations (etudiant, alumni...) ne sont PAS modifiées ici
        profil.setUserId(dto.getUserId());
        profil.setPhoto(dto.getPhoto());
        profil.setLienLinkedIn(dto.getLienLinkedIn());
        profil.setBio(dto.getBio());
        profil.setLienGitHub(dto.getLienGitHub());
        profil.setPrenom(dto.getPrenom());
        profil.setTelephone(dto.getTelephone());
        profil.setAdresse(dto.getAdresse());
        profil.setVille(dto.getVille());
        profil.setPays(dto.getPays());
        profil.setCodePostal(dto.getCodePostal());
        profil.setSiteWeb(dto.getSiteWeb());
        profil.setDateNaissance(dto.getDateNaissance());
        profil.setGenre(dto.getGenre());
        profil.setNomProprietaire(dto.getNomProprietaire());

        Profil updated = profilRepository.save(profil);
        userRepository.findByEmail(updated.getUserId()).ifPresent(adminNotificationEventService::notifyProfileUpdated);
        return toDTO(updated);
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────
    @Override
    public void deleteProfil(Long id) {
        if (!profilRepository.existsById(id)) {
            throw new RuntimeException("Profil introuvable avec l'id : " + id);
        }
        profilRepository.deleteById(id);
    }
}