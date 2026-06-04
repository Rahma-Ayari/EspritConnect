package tn.esprit.espritconnect2.security;

import org.springframework.stereotype.Component;

@Component
public class UserAgentParser {

    public static class UserAgentDetails {
        public String os = "Inconnu";
        public String browser = "Inconnu";
        public String device = "Ordinateur";
    }

    public UserAgentDetails parse(String userAgent) {
        UserAgentDetails details = new UserAgentDetails();
        if (userAgent == null || userAgent.isEmpty()) {
            return details;
        }

        String ua = userAgent.toLowerCase();

        // OS Detection
        if (ua.contains("windows")) {
            details.os = "Windows";
        } else if (ua.contains("macintosh") || ua.contains("mac os x")) {
            details.os = "Mac OS";
        } else if (ua.contains("android")) {
            details.os = "Android";
            details.device = "Mobile";
        } else if (ua.contains("iphone") || ua.contains("ipad")) {
            details.os = "iOS";
            details.device = "Mobile";
        } else if (ua.contains("linux")) {
            details.os = "Linux";
        }

        // Browser Detection
        if (ua.contains("edg/")) {
            details.browser = "Edge";
        } else if (ua.contains("chrome/") && !ua.contains("chromium")) {
            details.browser = "Chrome";
        } else if (ua.contains("safari/") && ua.contains("version/")) {
            details.browser = "Safari";
        } else if (ua.contains("firefox/")) {
            details.browser = "Firefox";
        } else if (ua.contains("opera/") || ua.contains("opr/")) {
            details.browser = "Opera";
        }

        return details;
    }
}
