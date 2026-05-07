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

    @Test
    void consumeValidationSuccess_WithVehicle_CallsVehicleHandler() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        String message = "{\"userId\":\"" + userId + "\",\"vehicleId\":\"" + vehicleId + "\"}";
        DocumentValidatedEvent event = DocumentValidatedEvent.builder().userId(userId).vehicleId(vehicleId).build();

        when(objectMapper.readValue(message, DocumentValidatedEvent.class)).thenReturn(event);

        consumer.consumeValidationSuccessEvent(message);

        verify(profileService).handleValidationSuccess(userId);
        verify(profileService).handleVehicleValidationSuccess(vehicleId);
    }

    @Test
    void consumeValidationFailure_WithVehicle_CallsVehicleFailureHandler() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        String reason = "expired";
        String message = "{\"userId\":\"" + userId + "\",\"vehicleId\":\"" + vehicleId + "\",\"reason\":\"" + reason + "\"}";
        DocumentValidationFailedEvent event = DocumentValidationFailedEvent.builder()
                .userId(userId)
                .vehicleId(vehicleId)
                .reason(reason)
                .build();

        when(objectMapper.readValue(message, DocumentValidationFailedEvent.class)).thenReturn(event);

        consumer.consumeValidationFailureEvent(message);

        verify(profileService).handleValidationFailure(userId, reason);
        verify(profileService).handleVehicleValidationFailure(vehicleId, reason);
    }
}
