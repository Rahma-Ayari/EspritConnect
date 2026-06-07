package tn.esprit.espritconnect2.Config;

/**
 * API prefixes aligned with frontoffice (users) vs backoffice (admins).
 */
public final class ApiOfficePaths {

    public static final String FRONT = "/api/front";
    public static final String BACK = "/api/back";

    public static final String FRONT_SUPPORT = FRONT + "/support";
    public static final String BACK_SUPPORT = BACK + "/support";
    public static final String FRONT_MODERATION = FRONT + "/moderation";
    public static final String BACK_MODERATION = BACK + "/moderation";

    public static final String BACK_REPORTS = BACK + "/reports";
    public static final String BACK_MONITORING = BACK + "/monitoring";
    public static final String FRONT_CHATBOT = FRONT_SUPPORT + "/chatbot";

    private ApiOfficePaths() {
    }
}
