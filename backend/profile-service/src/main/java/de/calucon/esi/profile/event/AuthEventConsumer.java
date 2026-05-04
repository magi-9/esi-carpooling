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
public class AuthEventConsumer {

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-registration-topic", groupId = "profile-service")
    public void consumeUserRegisteredEvent(String message) {
        log.info("Received User Registration Event: {}", message);

        try {
            UserRegisteredEvent event = objectMapper.readValue(message, UserRegisteredEvent.class);

            if (event.getUserId() != null) {
                profileService.createInitialProfile(event.getUserId());
                log.info("Created initial skeleton profile for newly registered user: {}", event.getUserId());
            }
        } catch (Exception e) {
            log.error("Failed to process User Registration Event: {}", e.getMessage(), e);
        }
    }
}