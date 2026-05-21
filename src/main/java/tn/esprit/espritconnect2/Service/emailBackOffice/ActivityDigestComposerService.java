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
    private final PostRepository postRepository; // si tu l’ajoutes

    public String buildHtml(DigestConfig config, LocalDateTime windowStart, LocalDateTime windowEnd, String recipientName) {

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

        String bannerHtml = (config.getBannerUrl() != null && !config.getBannerUrl().isBlank())
                ? "<img src='" + config.getBannerUrl() + "' style='width:100%;max-height:182px;object-fit:cover;' alt='Banner'/>"
                : "<div style='background:#cc0000;height:120px;display:flex;align-items:center;justify-content:center;'>"
                + "<span style='color:#fff;font-size:22px;font-weight:700;font-family:Arial;'>EspritConnect</span></div>";

        // Template admin (builder)
        String adminHtml = config.getTemplateHtml() == null ? "" : config.getTemplateHtml();

        // Sections auto
        String autoSections = ""
                + sectionJobs(baseUrl, jobs)
                + sectionEvents(baseUrl, events)
                + sectionMembers(members)
                + sectionPosts(baseUrl, "Nouveaux posts", "/posts/", feedPosts)
                + sectionPosts(baseUrl, "Business directory", "/business/", businessPosts)
                + (config.isIncludePlatformContact()
                ? contactFooter()
                : "");

        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>Activity Digest</title>
            </head>
            <body style="margin:0;padding:0;background:#f5f5f5;">
              <div style="max-width:600px;margin:20px auto;background:#fff;border-radius:10px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,.08);font-family:Arial;">
                %s
                <div style="padding:22px 22px 10px;">
                  <h2 style="margin:0;color:#1a1a2e;font-size:18px;">Bonjour %s,</h2>
                  <p style="margin:8px 0 0;color:#6c757d;font-size:13px;line-height:1.5;">
                    Résumé automatique des nouveautés entre <b>%s</b> et <b>%s</b>.
                  </p>
                </div>
                <div style="padding:0 22px 10px;">
                  %s
                </div>
                <div style="padding:0 22px 24px;">
                  %s
                </div>
                <div style="background:#111827;color:#9ca3af;padding:14px 18px;font-size:12px;text-align:center;">
                  © EspritConnect — Email automatique
                </div>
              </div>
            </body>
            </html>
        """.formatted(
                bannerHtml,
                escape(recipientName),
                windowStart.toLocalDate(),
                windowEnd.toLocalDate(),
                adminHtml,
                autoSections
        );
    }

    private String sectionJobs(String baseUrl, List<Offre> jobs) {
        if (jobs.isEmpty()) return "";
        String items = jobs.stream().map(j ->
                "<li style='margin:6px 0;color:#111827;font-size:13px;'>" +
                        escape(j.getTitre()) +
                        " — <a style='color:#cc0000;text-decoration:none;' href='" + baseUrl + "/jobs/" + j.getIdOffre() + "'>voir</a></li>"
        ).reduce("", (a,b) -> a+b);

        return block("Latest job posts", items, baseUrl + "/offres");
    }

    private String sectionEvents(String baseUrl, List<Evenement> events) {
        if (events.isEmpty()) return "";
        String items = events.stream().map(e ->
                "<li style='margin:6px 0;color:#111827;font-size:13px;'>" +
                        escape(e.getTitre()) +
                        " — <a style='color:#cc0000;text-decoration:none;' href='" + baseUrl + "/events/" + e.getIdEvenement() + "'>voir</a></li>"
        ).reduce("", (a,b) -> a+b);

        return block("Latest Events", items, baseUrl + "/evenements");
    }

    private String sectionMembers(List<Etudiant> members) {
        if (members.isEmpty()) return "";
        String items = members.stream().map(m ->
                "<li style='margin:6px 0;color:#111827;font-size:13px;'>" + escape(m.getNom()) + "</li>"
        ).reduce("", (a,b) -> a+b);

        return block("Recently Joined Members", items, null);
    }

    private String sectionPosts(String baseUrl, String title, String linkPrefix, List<Post> posts) {
        if (posts == null || posts.isEmpty()) return "";
        String items = posts.stream().map(p ->
                "<li style='margin:6px 0;color:#111827;font-size:13px;'>" +
                        escape(p.getTitle()) +
                        " — <a style='color:#cc0000;text-decoration:none;' href='" + baseUrl + linkPrefix + p.getId() + "'>voir</a></li>"
        ).reduce("", (a,b) -> a+b);

        return block(title, items, baseUrl + linkPrefix);
    }

    private String block(String title, String listItems, String ctaUrl) {
        String cta = (ctaUrl == null) ? "" :
                "<a href='" + ctaUrl + "' style='display:inline-block;margin-top:10px;background:#cc0000;color:#fff;text-decoration:none;padding:10px 14px;border-radius:8px;font-size:13px;font-weight:700;'>Voir tout</a>";

        return """
            <div style="border:1px solid #e9ecef;border-radius:10px;padding:14px 14px;margin-top:14px;">
              <div style="font-weight:800;color:#1a1a2e;font-size:14px;margin-bottom:8px;">%s</div>
              <ul style="padding-left:18px;margin:0;">%s</ul>
              %s
            </div>
        """.formatted(escape(title), listItems, cta);
    }

    private String contactFooter() {
        return """
            <div style="margin-top:16px;padding:12px;border-top:1px solid #e9ecef;color:#6c757d;font-size:12px;">
              <div><b>Contact</b> : support@esprit.tn</div>
              <div>Tunis, Esprit — Honoris United Universities</div>
            </div>
        """;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}