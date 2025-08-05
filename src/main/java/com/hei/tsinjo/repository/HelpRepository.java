package com.hei.tsinjo.repository;

import com.hei.tsinjo.domain.model.Help;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface HelpRepository extends JpaRepository<Help, UUID> {
    @Query("SELECT h FROM Help h " +
            "JOIN FETCH h.beneficiary " +
            "JOIN FETCH h.payment " +
            "ORDER BY h.createdAt DESC")
    List<Help> findAllOrderByCreatedAtDesc();
}
