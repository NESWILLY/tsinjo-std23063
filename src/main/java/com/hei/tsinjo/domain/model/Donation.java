package com.hei.tsinjo.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
