package com.hei.tsinjo.domain.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "psp_payment_id", nullable = false)
    private String pspPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "psp_type", nullable = false)
    private PspType pspType = PspType.ORANGE_MONEY;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "payer_email", nullable = false)
    private String payerEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.VERIFYING;

    @Column(name = "verification_attempt_nb")
    private Integer verificationAttemptNb = 0;

    @Column(name = "creation_instant")
    private Instant creationInstant = Instant.now();

    @Column(name = "last_verification_instant")
    private Instant lastVerificationInstant;

    public enum PspType {
        ORANGE_MONEY
    }

    public enum VerificationStatus {
        VERIFYING,
        SUCCEEDED,
        FAILED
    }
}