package com.hei.tsinjo.repository;

import com.hei.tsinjo.domain.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {
    @Query("SELECT d FROM Donation d " +
            "JOIN FETCH d.donor " +
            "JOIN FETCH d.payment " +
            "WHERE d.payment.verificationStatus = 'SUCCEEDED' " +
            "ORDER BY d.createdAt DESC")
    List<Donation> findAllSuccessfulDonationsOrderByCreatedAtDesc();
}