package tn.esprit.espritconnect2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebStaticConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sert les fichiers du dossier local "uploads" comme des ressources HTTP
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}