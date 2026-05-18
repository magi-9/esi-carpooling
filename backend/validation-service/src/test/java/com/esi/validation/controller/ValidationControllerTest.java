package com.esi.validation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.List;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.esi.validation.dto.VerificationRequestDTO;
import com.esi.validation.service.ValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@WebMvcTest(ValidationController.class)
public class ValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ValidationService validationService;

    @Test
    void createValidation_happyPath_returnsCreatedAndBody() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID vehicleId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID requestId = UUID.randomUUID();

        String json = String.format("{\"userId\":\"%s\",\"vehicleId\":\"%s\",\"documents\":[{\"documentType\":\"ID\"}]}", userId, vehicleId);

        MockMultipartFile dataPart = new MockMultipartFile("data", "data.json", "application/json", json.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile filePart = new MockMultipartFile("files", "id.pdf", "application/pdf", "dummy".getBytes(StandardCharsets.UTF_8));

        VerificationRequestDTO expected = new VerificationRequestDTO();
        expected.setRequestId(requestId);
        expected.setUserId(userId);
        expected.setVehicleId(vehicleId);
        expected.setStatus("PENDING");
        expected.setIsApproved(Boolean.FALSE);

        when(validationService.createVerification(any(), anyList())).thenReturn(expected);

        mockMvc.perform(multipart("/validation").file(dataPart).file(filePart).contentType(MediaType.MULTIPART_FORM_DATA)
            .with(jwt().jwt(jwt -> jwt.subject(userId.toString()).claim("roles", List.of("DRIVER")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(requestId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.vehicleId").value(vehicleId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.isApproved").value(false));
    }

    @Test
    void createValidation_serviceThrows_returnsErrorStatus() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID vehicleId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        String json = String.format("{\"userId\":\"%s\",\"vehicleId\":\"%s\",\"documents\":[{\"documentType\":\"ID\"}]}", userId, vehicleId);

        MockMultipartFile dataPart = new MockMultipartFile("data", "data.json", "application/json", json.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile filePart = new MockMultipartFile("files", "id.pdf", "application/pdf", "dummy".getBytes(StandardCharsets.UTF_8));

        when(validationService.createVerification(any(), anyList()))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "downstream"));

        mockMvc.perform(multipart("/validation").file(dataPart).file(filePart).contentType(MediaType.MULTIPART_FORM_DATA)
            .with(jwt().jwt(jwt -> jwt.subject(userId.toString()).claim("roles", List.of("DRIVER")))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createValidation_missingData_returnsBadRequest() throws Exception {
        MockMultipartFile filePart = new MockMultipartFile("files", "id.pdf", "application/pdf", "dummy".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/validation").file(filePart).contentType(MediaType.MULTIPART_FORM_DATA)
            .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()).claim("roles", List.of("DRIVER")))))
                .andExpect(status().isBadRequest());

        verify(validationService, never()).createVerification(any(), anyList());
    }
}
