package com.esi.validation.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.esi.validation.event.EventPublisher;
import com.esi.validation.model.VerificationRequest;
import com.esi.validation.repository.VerificationRequestRepository;

import java.util.Objects;

@Component
public class ValidationProcessor {

    private final EventPublisher publisher;
    private final VerificationRequestRepository repository;

    public ValidationProcessor(EventPublisher publisher, VerificationRequestRepository repository) {
        this.publisher = publisher;
        this.repository = repository;
    }

    @Async
    public void processVerificationAsync(VerificationRequest request) {
        try {
            // Simulate long-running external verification (replace with real integration)
            Thread.sleep(3000);

            // Simple heuristic: if any document filename contains "fail" -> reject
            boolean hasFail = request.getDocuments().stream()
                    .filter(Objects::nonNull)
                    .anyMatch(d -> d.getFileName() != null && d.getFileName().toLowerCase().contains("fail"));

            if (!hasFail) {
                request.setStatus("COMPLETED");
                request.setIsApproved(Boolean.TRUE);
                repository.save(request);
                publisher.publishValidationSuccess(request.getUserId(), request.getVehicleId());
            } else {
                request.setStatus("COMPLETED");
                request.setIsApproved(Boolean.FALSE);
                repository.save(request);
                publisher.publishValidationFailure(request.getUserId(), request.getVehicleId(), "illegible");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Verification processing interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error during verification processing: " + e.getMessage());
        }
    }
}
