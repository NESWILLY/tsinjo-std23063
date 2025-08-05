package com.hei.tsinjo.repository;

import com.hei.tsinjo.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByPspPaymentId(String pspPaymentId);

    @Query("SELECT p FROM Payment p WHERE p.verificationStatus = 'VERIFYING'")
    List<Payment> findPendingVerifications();
}
