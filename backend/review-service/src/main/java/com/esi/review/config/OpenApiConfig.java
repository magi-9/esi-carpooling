package com.esi.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reviewServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Review Service API")
                        .description("Review management for completed rides in the carpooling platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ESI Carpooling Team")
                                .email("esi@carpooling.com")));
    }
}
