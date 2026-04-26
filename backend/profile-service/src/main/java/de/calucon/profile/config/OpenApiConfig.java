package de.calucon.profile.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI profileServiceOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8085");
        server.setDescription("Profile Service Local Development Server");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("Profile Service API")
                        .description(
                                "API for managing user profiles, driver statuses, and vehicle records in the carpooling system")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Simon Schwitz")
                                .email("simon.schwitz@esi.de")));
    }
}