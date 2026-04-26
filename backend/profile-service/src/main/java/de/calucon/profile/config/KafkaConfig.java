package de.calucon.profile.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String VALIDATION_EVENTS_TOPIC = "validation-events";

    @Bean
    public NewTopic validationEventsTopic() {
        return TopicBuilder.name(VALIDATION_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}