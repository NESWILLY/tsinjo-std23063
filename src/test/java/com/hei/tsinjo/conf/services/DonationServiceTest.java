package com.hei.tsinjo.conf.services;

import com.hei.tsinjo.domain.model.Donation;
import com.hei.tsinjo.domain.model.Donor;
import com.hei.tsinjo.domain.model.Payment;
import com.hei.tsinjo.repository.DonationRepository;
import com.hei.tsinjo.repository.DonorRepository;
import com.hei.tsinjo.repository.PaymentRepository;
import com.hei.tsinjo.service.DonationService;
import com.hei.tsinjo.service.PaymentVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentVerificationService paymentVerificationService;

    @InjectMocks
    private DonationService donationService;

    private Donor donor;
    private Payment payment;
    private Donation donation;

    @BeforeEach
    void setUp() {
        donor = new Donor();
        donor.setId(UUID.randomUUID());
        donor.setEmail("test@example.com");
        donor.setFullName("Test Donor");

        payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setPspPaymentId("MP123456");
        payment.setPayerEmail("test@example.com");
        payment.setVerificationStatus(Payment.VerificationStatus.VERIFYING);

        donation = new Donation();
        donation.setId(UUID.randomUUID());
        donation.setDonor(donor);
        donation.setPayment(payment);
    }

    @Test
    void createDonation_WithExistingDonor_ShouldCreateDonation() {
        // Given
        when(donorRepository.findByEmail("test@example.com")).thenReturn(Optional.of(donor));
        when(donorRepository.save(any(Donor.class))).thenReturn(donor);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(donationRepository.save(any(Donation.class))).thenReturn(donation);

        // When
        Donation result = donationService.createDonation("test@example.com", "Test Donor", "MP123456");

        // Then
        assertNotNull(result);
        assertEquals(donor, result.getDonor());
        assertEquals(payment, result.getPayment());

        verify(donorRepository).findByEmail("test@example.com");
        verify(paymentRepository).save(any(Payment.class));
        verify(donationRepository).save(any(Donation.class));
        verify(paymentVerificationService).startPaymentVerification(any(Payment.class));
    }

    @Test
    void createDonation_WithNewDonor_ShouldCreateDonorAndDonation() {
        // Given
        when(donorRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(donorRepository.save(any(Donor.class))).thenReturn(donor);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(donationRepository.save(any(Donation.class))).thenReturn(donation);

        // When
        Donation result = donationService.createDonation("new@example.com", "New Donor", "MP123456");

        // Then
        assertNotNull(result);
        verify(donorRepository).save(any(Donor.class));
        verify(paymentVerificationService).startPaymentVerification(any(Payment.class));
    }

    @Test
    void getAllSuccessfulDonations_ShouldReturnDonations() {
        // Given
        List<Donation> expectedDonations = Arrays.asList(donation);
        when(donationRepository.findAllSuccessfulDonationsOrderByCreatedAtDesc()).thenReturn(expectedDonations);

        // When
        List<Donation> result = donationService.getAllSuccessfulDonations();

        // Then
        assertEquals(expectedDonations, result);
        verify(donationRepository).findAllSuccessfulDonationsOrderByCreatedAtDesc();
    }
}
