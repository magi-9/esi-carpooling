package de.calucon.esi.profile.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ValidationClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String validationServiceUrl;

    public ValidationClient(@Value("${validation.service.url:http://localhost:8087}") String validationServiceUrl) {
        this.validationServiceUrl = validationServiceUrl;
    }

    public void requestDriverVerification(UUID userId) {
        sendValidationRequest(userId, null);
    }

    public void requestVehicleVerification(UUID userId, UUID vehicleId) {
        sendValidationRequest(userId, vehicleId);
    }

    private void sendValidationRequest(UUID userId, UUID vehicleId) {
        try {
            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("vehicleId", vehicleId);
            payload.put("documents", Collections.emptyList());

            HttpEntity<Map<String, Object>> dataPart = new HttpEntity<>(payload, jsonHeaders);

            MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
            multipartBody.add("data", dataPart);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartBody, headers);

            String url = validationServiceUrl.endsWith("/") ? validationServiceUrl + "validation"
                    : validationServiceUrl + "/validation";
            restTemplate.postForEntity(url, requestEntity, String.class);
            log.info("Sent validation request for userId={} vehicleId={} to {}", userId, vehicleId, url);
        } catch (Exception e) {
            log.warn("Failed to send validation request for userId={} vehicleId={}: {}", userId, vehicleId,
                    e.getMessage(), e);
        }
    }
}
