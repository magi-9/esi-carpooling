package de.calucon.esi.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply to all endpoints
                .allowedOrigins("http://localhost:3000") // Allow your frontend port(s)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // OPTIONS is crucial for the preflight
                                                                           // request
                .allowedHeaders("*") // Allow all headers (like Authorization for your JWT)
                .allowCredentials(true); // Required if you ever use cookies or specific auth flows
    }
}
