package de.calucon.esi.profile.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.calucon.esi.profile.service.ProfileService;

@ExtendWith(MockitoExtension.class)
class ValidationEventConsumerTest {

    @Mock
    private ProfileService profileService;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private ValidationEventConsumer consumer;

    @Test
    void consumeValidationSuccess_CallsService() throws Exception {
        UUID userId = UUID.randomUUID();
        String message = "{\"userId\":\"" + userId + "\"}";
        DocumentValidatedEvent event = DocumentValidatedEvent.builder().userId(userId).build();

        when(objectMapper.readValue(message, DocumentValidatedEvent.class)).thenReturn(event);

        consumer.consumeValidationSuccessEvent(message);

        verify(profileService).handleValidationSuccess(userId);
    }
}