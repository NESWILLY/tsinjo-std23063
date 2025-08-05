package com.hei.tsinjo.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "helps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Help {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "accident_description", nullable = false, columnDefinition = "TEXT")
    private String accidentDescription;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
