package com.hei.tsinjo.service;

import com.hei.tsinjo.client.VolaPaymentClient;
import com.hei.tsinjo.domain.model.Payment;
import com.hei.tsinjo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentVerificationService {

    private final VolaPaymentClient volaClient;
    private final PaymentRepository paymentRepository;

    @Async
    public void startPaymentVerification(Payment payment) {
        try {
            log.info("Starting payment verification for payment: {}", payment.getId());

            // Créer la vérification chez Vola
            VolaPaymentClient.VolaPaymentResponse response = volaClient.createPayment(
                    payment.getPayerEmail(),
                    payment.getPspPaymentId()
            );

            // Mettre à jour le payment avec les infos de Vola
            updatePaymentFromVolaResponse(payment, response);

        } catch (Exception e) {
            log.error("Error starting payment verification for payment: {}", payment.getId(), e);
            payment.setVerificationStatus(Payment.VerificationStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    @Scheduled(fixedRate = 30000) // Vérifier toutes les 30 secondes
    @Transactional
    public void checkPendingVerifications() {
        List<Payment> pendingPayments = paymentRepository.findPendingVerifications();

        log.info("Checking {} pending payment verifications", pendingPayments.size());

        for (Payment payment : pendingPayments) {
            try {
                checkPaymentStatus(payment);
            } catch (Exception e) {
                log.error("Error checking payment verification for payment: {}", payment.getId(), e);
                incrementVerificationAttempt(payment);
            }
        }
    }

    private void checkPaymentStatus(Payment payment) {
        log.debug("Checking payment status for payment: {}", payment.getId());

        VolaPaymentClient.VolaPaymentResponse response = volaClient.getPayment(
                payment.getPayerEmail(),
                payment.getPspPaymentId()
        );

        updatePaymentFromVolaResponse(payment, response);
    }

    private void updatePaymentFromVolaResponse(Payment payment, VolaPaymentClient.VolaPaymentResponse response) {
        // Mettre à jour le statut
        payment.setVerificationStatus(
                Payment.VerificationStatus.valueOf(response.getVerificationStatus())
        );

        // Mettre à jour les métadonnées de vérification
        payment.setLastVerificationInstant(Instant.now());
        payment.setVerificationAttemptNb(response.getVerificationAttemptNb());

        // Mettre à jour le montant si disponible
        if (response.getPspPayment() != null && response.getPspPayment().getAmount() != null) {
            payment.setAmount(response.getPspPayment().getAmount());
        }

        paymentRepository.save(payment);

        log.info("Payment {} updated with status: {}",
                payment.getId(), payment.getVerificationStatus());
    }

    private void incrementVerificationAttempt(Payment payment) {
        payment.setVerificationAttemptNb(payment.getVerificationAttemptNb() + 1);
        payment.setLastVerificationInstant(Instant.now());

        // Marquer comme échoué après 10 tentatives
        if (payment.getVerificationAttemptNb() >= 10) {
            payment.setVerificationStatus(Payment.VerificationStatus.FAILED);
            log.warn("Payment {} marked as FAILED after 10 verification attempts", payment.getId());
        }

        paymentRepository.save(payment);
    }
}
