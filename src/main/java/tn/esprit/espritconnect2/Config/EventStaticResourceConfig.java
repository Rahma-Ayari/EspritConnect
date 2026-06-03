package tn.esprit.espritconnect2.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class EventStaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.events-dir:uploads/events}")
    private String eventUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(eventUploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/events/**")
                .addResourceLocations(location);
    }
}
