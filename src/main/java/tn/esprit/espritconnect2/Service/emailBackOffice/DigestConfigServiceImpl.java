package tn.esprit.espritconnect2.Service.emailBackOffice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.emailBackOffice.*;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.DigestConfig;
import tn.esprit.espritconnect2.Repository.emailBackOffice.DigestConfigRepository;

@Service
@RequiredArgsConstructor
public class DigestConfigServiceImpl { // ← Gestion de la configuration

    private final DigestConfigRepository repo;
    private static final Long CONFIG_ID = 1L; // ← Config unique, ID toujours = 1

    // ── Méthode privée utilitaire : s'assure qu'une config existe ──
    private DigestConfig ensureConfig() {
        return repo.findById(CONFIG_ID).orElseGet(() -> {
            // Si pas de config en BDD → on en crée une avec les valeurs par défaut
            DigestConfig c = DigestConfig.builder()
                    .id(CONFIG_ID)
                    .sujet("What's new on Esprit")
                    .bannerUrl("")
                    .frequence("WEEKLY")
                    .actif(true)
                    .templateHtml(defaultTemplate())
                    .recentlyJoinedMembers(true)
                    .latestEvents(true)
                    .latestFeedPosts(true)
                    .latestJobPosts(true)
                    .includePlatformContact(true)
                    .frontendBaseUrl("http://localhost:4200")
                    .build();
            return repo.save(c); // ← Sauvegarde et retourne
        });
    }

    // ── Lire la configuration ──
    public DigestConfigResponseDTO getConfig() {
        return toDTO(ensureConfig()); // ← Convertit l'entité en DTO pour l'envoyer
    }

    // ── Modifier la configuration ──
    public DigestConfigResponseDTO updateConfig(DigestConfigRequestDTO dto) {
        DigestConfig c = ensureConfig(); // ← Récupère la config existante

        // Met à jour chaque champ
        c.setSujet(dto.getSujet());
        c.setBannerUrl(dto.getBannerUrl());
        c.setFrequence(dto.getFrequence());

        // Pour actif : si le DTO ne précise pas → on garde l'ancienne valeur
        c.setActif(dto.getActif() != null ? dto.getActif() : c.getActif());

        // Template HTML : seulement si l'admin en envoie un
        if (dto.getTemplateHtml() != null) c.setTemplateHtml(dto.getTemplateHtml());

        // URL frontend : seulement si non vide
        if (dto.getFrontendBaseUrl() != null && !dto.getFrontendBaseUrl().isBlank()) {
            c.setFrontendBaseUrl(dto.getFrontendBaseUrl());
        }

        // Sections (checkboxes)
        if (dto.getSections() != null) {
            DigestSectionsDTO s = dto.getSections();
            c.setBusinessDirectoryPosts(s.isBusinessDirectoryPosts());
            c.setRecentlyJoinedMembers(s.isRecentlyJoinedMembers());
            c.setLatestEvents(s.isLatestEvents());
            c.setLatestFeedPosts(s.isLatestFeedPosts());
            c.setLatestJobPosts(s.isLatestJobPosts());
            c.setIncludePlatformContact(s.isIncludePlatformContact());
        }

        return toDTO(repo.save(c)); // ← Sauvegarde en BDD et retourne le DTO
    }

    // ── Réinitialiser aux valeurs par défaut ──
    public DigestConfigResponseDTO resetToDefault() {
        DigestConfig c = ensureConfig();

        c.setSujet("What's new on Esprit");
        c.setBannerUrl("");
        c.setFrequence("WEEKLY");
        c.setActif(true);
        c.setTemplateHtml(defaultTemplate());
        c.setBusinessDirectoryPosts(false);
        c.setRecentlyJoinedMembers(true);
        c.setLatestEvents(true);
        c.setLatestFeedPosts(true);
        c.setLatestJobPosts(true);
        c.setIncludePlatformContact(true);
        c.setFrontendBaseUrl("http://localhost:4200");

        return toDTO(repo.save(c));
    }

    public DigestConfigResponseDTO clearTemplate() {
        DigestConfig c = ensureConfig();
        c.setSujet("");
        c.setBannerUrl("");
        c.setTemplateHtml("");
        return toDTO(repo.save(c));
    }

    private DigestConfigResponseDTO toDTO(DigestConfig c) {
        DigestSectionsDTO sections = DigestSectionsDTO.builder()
                .businessDirectoryPosts(c.isBusinessDirectoryPosts())
                .recentlyJoinedMembers(c.isRecentlyJoinedMembers())
                .latestEvents(c.isLatestEvents())
                .latestFeedPosts(c.isLatestFeedPosts())
                .latestJobPosts(c.isLatestJobPosts())
                .includePlatformContact(c.isIncludePlatformContact())
                .build();

        return DigestConfigResponseDTO.builder()
                .id(c.getId())
                .sujet(c.getSujet())
                .bannerUrl(c.getBannerUrl())
                .frequence(c.getFrequence())
                .actif(c.getActif())
                .templateHtml(c.getTemplateHtml())
                .sections(sections)
                .frontendBaseUrl(c.getFrontendBaseUrl())
                .lastSentAt(c.getLastSentAt() != null ? c.getLastSentAt().toString() : null)
                .build();
    }

    private String defaultTemplate() {
        // HTML “admin” (tu peux le remplacer par l’export Unlayer plus tard)
        return """
            <div style="padding:16px 0;">
              <h3 style="margin:0;color:#1a1a2e;font-family:Arial;">
                Les nouveautés de la plateforme
              </h3>
              <p style="color:#6c757d;font-family:Arial;margin:8px 0 0;">
                Voici un résumé automatique. Clique sur les boutons pour voir les détails.
              </p>
            </div>
        """;
    }
}