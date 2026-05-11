package com.esi.validation.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishValidationSuccess(UUID userId, UUID vehicleId) {
        try {
            DocumentValidatedEvent event = new DocumentValidatedEvent(userId, vehicleId);
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("document-validated-topic", payload);
        } catch (Exception e) {
            System.out.println("Failed to publish validation success event: " + e.getMessage());
        }
    }

    public void publishValidationFailure(UUID userId, UUID vehicleId, String reason) {
        try {
            DocumentValidationFailedEvent event = new DocumentValidationFailedEvent(userId, vehicleId, reason);
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("document-validation-failed-topic", payload);
        } catch (Exception e) {
            System.out.println("Failed to publish validation failure event: " + e.getMessage());
        }
    }
}
