package tn.esprit.espritconnect2.Service.emailBackOffice;

import org.springframework.stereotype.Component;

/**
 * Shared HTML email layout matching the Activity Digest / ESPRIT Connect branding.
 */
@Component
public class EspritConnectEmailTemplate {

    public String build(String recipientName, String introParagraphsHtml, String cardHtml, String ctaUrl, String ctaLabel) {
        String safeName = recipientName == null || recipientName.isBlank() ? "there" : escape(recipientName);
        String cta = "";
        if (ctaUrl != null && !ctaUrl.isBlank() && ctaLabel != null && !ctaLabel.isBlank()) {
            cta = """
                <div style="margin-top:18px; text-align:center;">
                  <a href="%s" style="display:inline-block; background-color:#dc2626; color:#ffffff; text-decoration:none; padding:12px 24px; border-radius:8px; font-size:14px; font-weight:700;">%s</a>
                </div>
                """.formatted(escapeAttr(ctaUrl), escape(ctaLabel));
        }

        String card = cardHtml == null ? "" : """
            <div style="border:1px solid #e5e7eb; border-radius:10px; padding:18px 20px; margin-top:20px; background-color:#f9fafb;">
              %s
              %s
            </div>
            """.formatted(cardHtml, cta);

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>ESPRIT Connect</title>
            </head>
            <body style="margin:0; padding:0; background-color:#f3f4f6; -webkit-font-smoothing:antialiased;">
              <div style="width:100%%; max-width:600px; margin:20px auto; background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.05); border:1px solid #e5e7eb; font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                %s
                <div style="padding:32px 32px 20px;">
                  <h2 style="margin:0; color:#111827; font-size:20px; font-weight:700;">Hello %s,</h2>
                  <div style="margin:10px 0 0; color:#4b5563; font-size:14px; line-height:1.6;">
                    %s
                  </div>
                </div>
                <div style="padding:0 32px 32px;">
                  %s
                  %s
                </div>
                <div style="background:linear-gradient(135deg, #dc2626 0%%, #b91c1c 100%%); color:#ffe4e6; padding:24px 24px; font-size:12px; text-align:center; font-family:Arial, sans-serif; border-top:1px solid #fecaca;">
                  <div style="font-weight:600; color:#ffffff; margin-bottom:6px;">ESPRIT Connect</div>
                  <div style="margin-bottom:12px; opacity:0.8;">You are receiving this email because you are registered on the ESPRIT Connect platform.</div>
                  <div style="border-top:1px solid rgba(255,255,255,0.25); padding-top:12px; opacity:0.9;">
                    © 2026 ESPRIT — Honoris United Universities. All rights reserved.
                  </div>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                banner(),
                safeName,
                introParagraphsHtml == null ? "" : introParagraphsHtml,
                card,
                contactFooter()
        );
    }

    public String cardTitle(String title) {
        return """
            <div style="font-weight:700; color:#111827; font-size:15px; margin-bottom:12px; border-left:3px solid #dc2626; padding-left:10px;">%s</div>
            """.formatted(escape(title));
    }

    public String cardBody(String html) {
        return "<div style=\"color:#374151; font-size:14px; line-height:1.6;\">" + html + "</div>";
    }

    private String banner() {
        return """
            <div style="background:linear-gradient(135deg, #dc2626 0%, #b91c1c 100%); padding:40px 24px; text-align:center; color:#ffffff;">
              <div style="font-size:32px; font-weight:800; letter-spacing:1px; margin:0; font-family:Arial, sans-serif;">ESPRIT<span style="color:#ffd2d2;">Connect</span></div>
              <div style="font-size:14px; opacity:0.85; margin-top:6px; font-family:Arial, sans-serif;">Learn Differently</div>
              <div style="font-size:11px; opacity:0.75; margin-top:14px; letter-spacing:0.08em; text-transform:uppercase;">Honoris United Universities</div>
            </div>
            """;
    }

    private String contactFooter() {
        return """
            <div style="margin-top:24px; padding:16px; border-top:1px dashed #e5e7eb; color:#6b7280; font-size:12.5px; line-height:1.5; background-color:#f9fafb; border-radius:8px;">
              <div style="font-weight:700; color:#374151; margin-bottom:4px;">Contact details:</div>
              <div><b>Email</b>: <a href="mailto:support.connect@esprit.tn" style="color:#dc2626; text-decoration:none;">support.connect@esprit.tn</a></div>
              <div><b>Address</b>: Tunis, Esprit — Honoris United Universities</div>
            </div>
            """;
    }

    private String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String escapeAttr(String value) {
        return escape(value).replace("'", "&#39;");
    }
}
