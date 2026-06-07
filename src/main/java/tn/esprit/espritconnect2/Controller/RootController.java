package tn.esprit.espritconnect2.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "service", "EspritConnect2 API",
                "docs", "/swagger-ui.html",
                "auth", "/api/auth/login",
                "frontoffice", "/api/front",
                "backoffice", "/api/back"
        );
    }
}
