package de.calucon.esi.profile.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.calucon.esi.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationEventConsumer {

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    // Use your KafkaConfig constants for the topics if you prefer
    @KafkaListener(topics = "document-validated-topic", groupId = "profile-service")
    public void consumeValidationSuccessEvent(String message) {
        log.info("Received Validation Success Event: {}", message);

        try {
            DocumentValidatedEvent event = objectMapper.readValue(message, DocumentValidatedEvent.class);

            if (event.getUserId() != null) {
                profileService.handleValidationSuccess(event.getUserId());
                log.info("Successfully updated driver status to VERIFIED for user: {}", event.getUserId());
            }
        } catch (Exception e) {
            log.error("Failed to process Validation Success Event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "document-validation-failed-topic", groupId = "profile-service")
    public void consumeValidationFailureEvent(String message) {
        log.info("Received Validation Failure Event: {}", message);

        try {
            DocumentValidationFailedEvent event = objectMapper.readValue(message, DocumentValidationFailedEvent.class);

            if (event.getUserId() != null) {
                profileService.handleValidationFailure(event.getUserId(), event.getReason());
                log.warn("Updated driver status to REJECTED and flagged account for user: {}. Reason: {}",
                        event.getUserId(), event.getReason());
            }
        } catch (Exception e) {
            log.error("Failed to process Validation Failure Event: {}", e.getMessage(), e);
        }
    }
}