package com.esi.review.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.esi.review.exception.ServiceUnavailableException;

@Component
public class AuthServiceClient {

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Value("${clients.auth-service-url}")
    private String authServiceUrl;

    public void validateToken(String authHeader) {
        try {
            ResponseEntity<Void> response = restClientBuilder
                    .baseUrl(authServiceUrl)
                    .build()
                    .get()
                    .uri("/auth/validate")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .toBodilessEntity();
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Invalid token");
            }
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException("Auth service unavailable", e);
        } catch (Exception e) {
            throw new RuntimeException("Token validation failed: " + e.getMessage());
        }
    }
}
