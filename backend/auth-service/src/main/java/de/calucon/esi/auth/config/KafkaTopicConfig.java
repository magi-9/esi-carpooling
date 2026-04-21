package de.calucon.esi.auth.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import de.calucon.esi.auth.event.UserEventProducer;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic UserRegistrationTopicCreation() {
        return TopicBuilder.name(UserEventProducer.TOPIC)
                .build();
    }
}
