package com.esi.payment.infrastructure;

import com.esi.payment.domain.Payment;
import com.esi.payment.domain.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaPaymentRepository implements PaymentRepository {

    private final SpringDataPaymentRepository springDataRepo;
    private final PaymentMapper mapper;

    public JpaPaymentRepository(SpringDataPaymentRepository springDataRepo, PaymentMapper mapper) {
        this.springDataRepo = springDataRepo;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = mapper.toJpa(payment);
        PaymentJpaEntity saved = springDataRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return springDataRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByBookingId(String bookingId) {
        return springDataRepo.findByBookingId(bookingId).map(mapper::toDomain);
    }
}
