package tn.esprit.espritconnect2.Service.emailBackOffice;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.DigestConfig;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Builds Activity Digest HTML. Uses JDBC instead of JPA entities so this module
 * compiles even when legacy entity classes are unavailable in the IDE classpath.
 */
@Service
@RequiredArgsConstructor
public class ActivityDigestComposerService {

    private final JdbcTemplate jdbcTemplate;

    public String buildHtml(DigestConfig config, LocalDateTime windowStart, LocalDateTime windowEnd, String recipientName, boolean isForEmail) {
        String baseUrl = config.getFrontendBaseUrl();
        Timestamp afterTs = Timestamp.valueOf(windowStart);

        String autoSections = ""
                + sectionJobs(baseUrl, config.isLatestJobPosts(), afterTs)
                + sectionEvents(baseUrl, config.isLatestEvents(), afterTs)
                + sectionMembers(config.isRecentlyJoinedMembers(), afterTs)
                + sectionPosts(baseUrl, "Nouveaux posts sur le fil", "/posts/", config.isLatestFeedPosts(), "FEED", windowStart)
                + sectionPosts(baseUrl, "Business Directory", "/business/", config.isBusinessDirectoryPosts(), "BUSINESS_DIRECTORY", windowStart)
                + (config.isIncludePlatformContact() ? contactFooter() : "");

        String bannerHtml = buildBannerHtml(config, isForEmail);
        String adminHtml = config.getTemplateHtml() == null ? "" : config.getTemplateHtml();

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
                %s
                <div style="padding:32px 32px 20px;">
                  <h2 style="margin:0; color:#111827; font-size:20px; font-weight:700;">Bonjour %s,</h2>
                  <p style="margin:10px 0 0; color:#4b5563; font-size:14px; line-height:1.6;">
                    Voici votre récapitulatif des actualités et opportunités partagées sur <b>ESPRIT Connect</b> pour la période du <b>%s</b> au <b>%s</b>.
                  </p>
                </div>
                <div style="padding:0 32px 10px; color:#374151; font-size:14px; line-height:1.6;">
                  %s
                </div>
                <div style="padding:0 32px 32px;">
                  %s
                </div>
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

    private String buildBannerHtml(DigestConfig config, boolean isForEmail) {
        if (config.getBannerUrl() != null && !config.getBannerUrl().isBlank()) {
            String src = isForEmail ? "cid:bannerImage" : config.getBannerUrl();
            return "<div style=\"width:100%; text-align:center; background-color:#ffffff; border-bottom:1px solid #e5e7eb; padding:0; margin:0; line-height:0;\">"
                    + "<img src=\"" + src + "\" width=\"600\" alt=\"Banner\" style=\"display:block; width:100%; max-width:600px; height:auto !important; border:none; margin:0 auto; outline:none; text-decoration:none;\" border=\"0\" />"
                    + "</div>";
        }
        return "<div style='background:linear-gradient(135deg, #dc2626 0%, #b91c1c 100%); padding:40px 24px; text-align:center; color:#ffffff;'>"
                + "<div style='font-size:32px; font-weight:800; letter-spacing:1px; margin:0; font-family:Arial, sans-serif;'>ESPRIT<span style='color:#ffd2d2;'>Connect</span></div>"
                + "<div style='font-size:14px; opacity:0.85; margin-top:6px; font-family:Arial, sans-serif;'>Se former autrement</div>"
                + "</div>";
    }

    private String sectionJobs(String baseUrl, boolean enabled, Timestamp after) {
        if (!enabled) {
            return "";
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id_offre, titre FROM offre WHERE date_publication > ? ORDER BY date_publication DESC LIMIT 10",
                    after);
            if (rows.isEmpty()) {
                return "";
            }
            String items = rows.stream().map(row ->
                    "<li style='margin:8px 0; color:#374151; font-size:13.5px; line-height:1.4;'>" +
                            "<strong style='color:#111827;'>" + escape(stringVal(row.get("titre"))) + "</strong>" +
                            " — <a style='color:#dc2626; text-decoration:none; font-weight:500;' href='" + baseUrl + "/jobs/" + row.get("id_offre") + "'>Consulter l'offre →</a></li>"
            ).reduce("", String::concat);
            return block("Dernières opportunités (Emplois & Stages)", items, baseUrl + "/offres");
        } catch (Exception ex) {
            return "";
        }
    }

    private String sectionEvents(String baseUrl, boolean enabled, Timestamp after) {
        if (!enabled) {
            return "";
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id_evenement, titre FROM evenement WHERE date_evenement > ? ORDER BY date_evenement DESC LIMIT 10",
                    after);
            if (rows.isEmpty()) {
                return "";
            }
            String items = rows.stream().map(row ->
                    "<li style='margin:8px 0; color:#374151; font-size:13.5px; line-height:1.4;'>" +
                            "<strong style='color:#111827;'>" + escape(stringVal(row.get("titre"))) + "</strong>" +
                            " — <a style='color:#dc2626; text-decoration:none; font-weight:500;' href='" + baseUrl + "/events/" + row.get("id_evenement") + "'>Détails de l'événement →</a></li>"
            ).reduce("", String::concat);
            return block("Événements à venir", items, baseUrl + "/evenements");
        } catch (Exception ex) {
            return "";
        }
    }

    private String sectionMembers(boolean enabled, Timestamp after) {
        if (!enabled) {
            return "";
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT nom FROM users WHERE enabled = 1 AND created_at > ? ORDER BY created_at DESC LIMIT 10",
                    after);
            if (rows.isEmpty()) {
                return "";
            }
            String items = rows.stream().map(row ->
                    "<li style='margin:6px 0; color:#374151; font-size:13.5px;'>" + escape(stringVal(row.get("nom"))) + "</li>"
            ).reduce("", String::concat);
            return block("Nouveaux membres inscrits", items, null);
        } catch (Exception ex) {
            return "";
        }
    }

    private String sectionPosts(String baseUrl, String title, String linkPrefix, boolean enabled, String category, LocalDateTime after) {
        if (!enabled) {
            return "";
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, title FROM post WHERE category = ? AND created_at > ? ORDER BY created_at DESC LIMIT 10",
                    category, Timestamp.valueOf(after));
            if (rows.isEmpty()) {
                return "";
            }
            String items = rows.stream().map(row ->
                    "<li style='margin:8px 0; color:#374151; font-size:13.5px; line-height:1.4;'>" +
                            "<strong style='color:#111827;'>" + escape(stringVal(row.get("title"))) + "</strong>" +
                            " — <a style='color:#dc2626; text-decoration:none; font-weight:500;' href='" + baseUrl + linkPrefix + row.get("id") + "'>Lire la publication →</a></li>"
            ).reduce("", String::concat);
            return block(title, items, baseUrl + linkPrefix);
        } catch (Exception ex) {
            return "";
        }
    }

    private String block(String title, String listItems, String ctaUrl) {
        String cta = (ctaUrl == null) ? "" :
                "<div style='margin-top:14px; text-align:right;'>" +
                "<a href='" + ctaUrl + "' style='display:inline-block; background-color:#dc2626; color:#ffffff; text-decoration:none; padding:8px 16px; border-radius:6px; font-size:13px; font-weight:700;'>Voir tout sur la plateforme</a>" +
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

    private String stringVal(Object value) {
        return value == null ? "" : value.toString();
    }

    private String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
