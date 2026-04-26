package de.calucon.profile.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.calucon.profile.config.KafkaConfig;
import de.calucon.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationEventConsumer {

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.VALIDATION_EVENTS_TOPIC, groupId = "profile-service")
    public void consumeValidationEvent(String message) {
        log.info("Received validation event: {}", message);

        try {
            DocumentValidatedEvent event = objectMapper.readValue(message, DocumentValidatedEvent.class);

            if (event.getUserId() != null) {
                profileService.updateDriverStatusFromValidation(event.getUserId(), event.isApproved());
                log.info("Updated driver status for user {} to {}",
                        event.getUserId(),
                        event.isApproved() ? "VERIFIED" : "REJECTED");
            }
        } catch (Exception e) {
            log.error("Failed to process validation event: {}", e.getMessage(), e);
        }
    }
}