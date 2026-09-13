package com.njiafix.repository;

import com.njiafix.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentRef(String paymentRef);
    boolean existsByPaymentRef(String paymentRef);
}
