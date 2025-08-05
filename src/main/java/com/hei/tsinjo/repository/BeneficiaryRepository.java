package com.hei.tsinjo.repository;

import com.hei.tsinjo.domain.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
    Optional<Beneficiary> findByEmail(String email);
}
