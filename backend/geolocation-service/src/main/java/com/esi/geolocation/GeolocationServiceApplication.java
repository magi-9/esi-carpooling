package com.esi.geolocation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class GeolocationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeolocationServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(@Value("${nominatim.user-agent:esi-geolocation-service/0.0 (contact:dev@localhost)}") String userAgent) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            if (!request.getHeaders().containsKey("User-Agent")) {
                request.getHeaders().add("User-Agent", userAgent);
            }
            // also set a reasonable Accept header
            if (!request.getHeaders().containsKey("Accept")) {
                request.getHeaders().add("Accept", "application/json");
            }
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
