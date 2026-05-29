package tn.esprit.espritconnect2.Service.emailBackOffice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.DigestConfig;
import tn.esprit.espritconnect2.Repository.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityDigestComposerService {

    private final OffreRepository offreRepository;
    private final EvenementRepository evenementRepository;
    private final EtudiantRepository etudiantRepository;
    private final PostRepository postRepository;

    public String buildHtml(DigestConfig config, LocalDateTime windowStart, LocalDateTime windowEnd, String recipientName, boolean isForEmail) {

        String baseUrl = config.getFrontendBaseUrl();
        Date afterDate = Date.from(windowStart.atZone(ZoneId.systemDefault()).toInstant());

        List<Offre> jobs = config.isLatestJobPosts()
                ? offreRepository.findTop10ByDatePublicationAfterOrderByDatePublicationDesc(afterDate)
                : List.of();

        List<Evenement> events = config.isLatestEvents()
                ? evenementRepository.findTop10ByDateEvenementAfterOrderByDateEvenementDesc(afterDate)
                : List.of();

        List<Etudiant> members = config.isRecentlyJoinedMembers()
                ? etudiantRepository.findTop10ByDateInscriptionAfterOrderByDateInscriptionDesc(afterDate)
                : List.of();

        List<Post> feedPosts = config.isLatestFeedPosts()
                ? postRepository.findTop10ByCategoryAndCreatedAtAfterOrderByCreatedAtDesc("FEED", windowStart)
                : List.of();

        List<Post> businessPosts = config.isBusinessDirectoryPosts()
                ? postRepository.findTop10ByCategoryAndCreatedAtAfterOrderByCreatedAtDesc("BUSINESS_DIRECTORY", windowStart)
                : List.of();

        // Banner HTML: display the uploaded banner if it exists, otherwise display a beautiful fallback banner
        String bannerHtml;
        if (config.getBannerUrl() != null && !config.getBannerUrl().isBlank()) {
            String src = isForEmail ? "cid:bannerImage" : config.getBannerUrl();
            bannerHtml = "<div style=\"width:100%; text-align:center; background-color:#ffffff; border-bottom:1px solid #e5e7eb; padding:0; margin:0; line-height:0;\">"
                    + "<img src=\"" + src + "\" width=\"600\" alt=\"Banner\" style=\"display:block; width:100%; max-width:600px; height:auto !important; border:none; margin:0 auto; outline:none; text-decoration:none;\" border=\"0\" />"
                    + "</div>";
        } else {
            bannerHtml = "<div style='background:linear-gradient(135deg, #dc2626 0%, #b91c1c 100%); padding:40px 24px; text-align:center; color:#ffffff;'>"
                    + "<div style='font-size:32px; font-weight:800; letter-spacing:1px; margin:0; font-family:Arial, sans-serif;'>ESPRIT<span style='color:#ffd2d2;'>Connect</span></div>"
                    + "<div style='font-size:14px; opacity:0.85; margin-top:6px; font-family:Arial, sans-serif;'>Se former autrement</div>"
                    + "</div>";
        }

        // Custom template HTML added by admin
        String adminHtml = config.getTemplateHtml() == null ? "" : config.getTemplateHtml();

        // Automatically generated sections
        String autoSections = ""
                + sectionJobs(baseUrl, jobs)
                + sectionEvents(baseUrl, events)
                + sectionMembers(members)
                + sectionPosts(baseUrl, "Nouveaux posts sur le fil", "/posts/", feedPosts)
                + sectionPosts(baseUrl, "Business Directory", "/business/", businessPosts)
                + (config.isIncludePlatformContact() ? contactFooter() : "");

        String htmlTemplate = """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>Activity Digest</title>
            </head>
            <body style="margin:0; padding:0; background-color:#f3f4f6; -webkit-font-smoothing:antialiased;">
              <div style="width:100%; max-width:600px; margin:20px auto; background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.05); border:1px solid #e5e7eb; font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                
                <!-- En-tête / Bannière -->
                %s
                
                <!-- Corps de l'email -->
                <div style="padding:32px 32px 20px;">
                  <h2 style="margin:0; color:#111827; font-size:20px; font-weight:700;">Bonjour %s,</h2>
                  <p style="margin:10px 0 0; color:#4b5563; font-size:14px; line-height:1.6;">
                    Voici votre récapitulatif des actualités et opportunités partagées sur <b>ESPRIT Connect</b> pour la période du <b>%s</b> au <b>%s</b>.
                  </p>
                </div>
                
                <!-- Contenu custom admin -->
                <div style="padding:0 32px 10px; color:#374151; font-size:14px; line-height:1.6;">
                  %s
                </div>
                
                <!-- Sections générées -->
                <div style="padding:0 32px 32px;">
                  %s
                </div>
                
                <!-- Pied de page -->
                <div style="background:linear-gradient(135deg, #dc2626 0%, #b91c1c 100%); color:#ffe4e6; padding:24px 24px; font-size:12px; text-align:center; font-family:Arial, sans-serif; border-top:1px solid #fecaca;">
                  <div style="font-weight:600; color:#ffffff; margin-bottom:6px;">ESPRIT Connect</div>
                  <div style="margin-bottom:12px; opacity:0.8;">Vous recevez cet email car vous êtes inscrit sur la plateforme ESPRIT Connect.</div>
                  <div style="border-top:1px solid rgba(255,255,255,0.25); padding-top:12px; opacity:0.9;">
                    © 2026 ESPRIT — Honoris United Universities. Tous droits réservés.
                  </div>
                </div>
                
              </div>
            </body>
            </html>
        """;

        return htmlTemplate
                .replace("%s", "%%s")
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(bannerHtml))
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(escape(recipientName)))
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(windowStart.toLocalDate().toString()))
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(windowEnd.toLocalDate().toString()))
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(adminHtml))
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(autoSections));
    }

    private String sectionJobs(String baseUrl, List<Offre> jobs) {
        if (jobs.isEmpty()) return "";
        String items = jobs.stream().map(j ->
                "<li style='margin:8px 0; color:#374151; font-size:13.5px; line-height:1.4;'>" +
                        "<strong style='color:#111827;'>" + escape(j.getTitre()) + "</strong>" +
                        " — <a style='color:#dc2626; text-decoration:none; font-weight:500;' href='" + baseUrl + "/jobs/" + j.getIdOffre() + "'>Consulter l'offre →</a></li>"
        ).reduce("", (a,b) -> a+b);

        return block("Dernières opportunités (Emplois & Stages)", items, baseUrl + "/offres");
    }

    private String sectionEvents(String baseUrl, List<Evenement> events) {
        if (events.isEmpty()) return "";
        String items = events.stream().map(e ->
                "<li style='margin:8px 0; color:#374151; font-size:13.5px; line-height:1.4;'>" +
                        "<strong style='color:#111827;'>" + escape(e.getTitre()) + "</strong>" +
                        " — <a style='color:#dc2626; text-decoration:none; font-weight:500;' href='" + baseUrl + "/events/" + e.getIdEvenement() + "'>Détails de l'événement →</a></li>"
        ).reduce("", (a,b) -> a+b);

        return block("Événements à venir", items, baseUrl + "/evenements");
    }

    private String sectionMembers(List<Etudiant> members) {
        if (members.isEmpty()) return "";
        String items = members.stream().map(m ->
                "<li style='margin:6px 0; color:#374151; font-size:13.5px;'>" + escape(m.getNom()) + "</li>"
        ).reduce("", (a,b) -> a+b);

        return block("Nouveaux membres inscrits", items, null);
    }

    private String sectionPosts(String baseUrl, String title, String linkPrefix, List<Post> posts) {
        if (posts == null || posts.isEmpty()) return "";
        String items = posts.stream().map(p ->
                "<li style='margin:8px 0; color:#374151; font-size:13.5px; line-height:1.4;'>" +
                        "<strong style='color:#111827;'>" + escape(p.getTitle()) + "</strong>" +
                        " — <a style='color:#dc2626; text-decoration:none; font-weight:500;' href='" + baseUrl + linkPrefix + p.getId() + "'>Lire la publication →</a></li>"
        ).reduce("", (a,b) -> a+b);

        return block(title, items, baseUrl + linkPrefix);
    }

    private String block(String title, String listItems, String ctaUrl) {
        String cta = (ctaUrl == null) ? "" :
                "<div style='margin-top:14px; text-align:right;'>" +
                "<a href='" + ctaUrl + "' style='display:inline-block; background-color:#dc2626; color:#ffffff; text-decoration:none; padding:8px 16px; border-radius:6px; font-size:13px; font-weight:700; transition:background-color 0.2s;'>Voir tout sur la plateforme</a>" +
                "</div>";

        return """
            <div style="border:1px solid #e5e7eb; border-radius:10px; padding:18px 20px; margin-top:20px; background-color:#f9fafb;">
              <div style="font-weight:700; color:#111827; font-size:15px; margin-bottom:12px; border-left:3px solid #dc2626; padding-left:10px;">%s</div>
              <ul style="padding-left:16px; margin:0; list-style-type:square; color:#9ca3af;">%s</ul>
              %s
            </div>
        """.formatted(escape(title), listItems, cta);
    }

    private String contactFooter() {
        return """
            <div style="margin-top:24px; padding:16px; border-top:1px dashed #e5e7eb; color:#6b7280; font-size:12.5px; line-height:1.5; background-color:#f9fafb; border-radius:8px;">
              <div style="font-weight:700; color:#374151; margin-bottom:4px;">Coordonnées de contact :</div>
              <div><b>Email</b> : support.connect@esprit.tn</div>
              <div><b>Adresse</b> : Tunis, Esprit — Honoris United Universities</div>
            </div>
        """;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}