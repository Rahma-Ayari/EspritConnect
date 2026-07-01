package tn.esprit.espritconnect2.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Sert les fichiers uploades via URL :
 * http://localhost:8089/espritconnect/uploads/banners/nom-fichier
 * <p>
 * Stockage disque : sous-dossiers de app.upload.root (ex. banners/)
 * URL publique : /uploads/**
 */
@Configuration
public class UploadStaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.root}")
    private String uploadRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convertit le chemin absolu en URI file:.../ (exemple Windows : file:///C:/Users/.../uploads/)
        String location = Paths.get(uploadRoot).toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }

        // With context path /espritconnect, the handler pattern should be /uploads/**
        // This will match requests to /espritconnect/uploads/**
        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600); // Cache for 1 hour
    }
}