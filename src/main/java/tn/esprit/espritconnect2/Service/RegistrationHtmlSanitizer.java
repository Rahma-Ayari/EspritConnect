package tn.esprit.espritconnect2.Service;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Strips scripts and unsafe markup from admin-provided registration legal HTML.
 */
public final class RegistrationHtmlSanitizer {

    private static final int MAX_CHARS = 500_000;

    private RegistrationHtmlSanitizer() {
    }

    public static String sanitize(String raw) {
        if (raw == null) {
            return "";
        }
        if (raw.length() > MAX_CHARS) {
            throw new IllegalArgumentException(
                    "Terms and privacy content exceeds maximum length (" + MAX_CHARS + " characters)");
        }
        return Jsoup.clean(raw, Safelist.relaxed());
    }
}
