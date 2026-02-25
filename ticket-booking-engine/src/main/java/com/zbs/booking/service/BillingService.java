package com.zbs.booking.service;

import com.zbs.booking.domain.Payment;
import com.zbs.booking.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment processPayment(Payment payment) {
        // Demonstrates polymorphic entity mapping persistence
        return paymentRepository.save(payment);
    }
}