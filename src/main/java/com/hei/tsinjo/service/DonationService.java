package com.hei.tsinjo.service;

import com.hei.tsinjo.domain.model.Donation;
import com.hei.tsinjo.domain.model.Donor;
import com.hei.tsinjo.domain.model.Payment;
import com.hei.tsinjo.repository.DonationRepository;
import com.hei.tsinjo.repository.DonorRepository;
import com.hei.tsinjo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonorRepository donorRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentVerificationService paymentVerificationService;

    @Transactional
    public Donation createDonation(String donorEmail, String donorFullName, String pspPaymentId) {
        log.info("Creating donation from donor: {} with payment: {}", donorEmail, pspPaymentId);

        // Créer ou récupérer le donateur
        Donor donor = donorRepository.findByEmail(donorEmail)
                .orElse(new Donor(null, donorEmail, donorFullName, null));
        donor = donorRepository.save(donor);

        // Créer le paiement
        Payment payment = new Payment();
        payment.setPspPaymentId(pspPaymentId);
        payment.setPayerEmail(donorEmail);
        payment.setPspType(Payment.PspType.ORANGE_MONEY);
        payment = paymentRepository.save(payment);

        // Créer la donation
        Donation donation = new Donation();
        donation.setDonor(donor);
        donation.setPayment(payment);
        donation = donationRepository.save(donation);

        // Démarrer la vérification du paiement
        paymentVerificationService.startPaymentVerification(payment);

        log.info("Donation created with ID: {}", donation.getId());
        return donation;
    }

    public List<Donation> getAllSuccessfulDonations() {
        return donationRepository.findAllSuccessfulDonationsOrderByCreatedAtDesc();
    }
}
