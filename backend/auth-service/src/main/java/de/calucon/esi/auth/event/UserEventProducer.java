package de.calucon.esi.auth.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    // The KafkaTemplate does all the heavy lifting
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    // Define the topic name as a constant so we don't make typos
    public static final String TOPIC = "user-registration-topic";

    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Publishing UserRegisteredEvent to Kafka for User ID: {}", event.getUserId());

        // We use the User ID as the Kafka "Key" so all events for the same user go to
        // the same partition
        kafkaTemplate
                .send(TOPIC, event.getUserId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish UserRegisteredEvent for User ID: {}. Error: {}",
                                event.getUserId(), ex.getMessage());
                    } else {
                        log.debug("Successfully published UserRegisteredEvent for User ID: {}",
                                event.getUserId());
                    }
                });

    }
}
