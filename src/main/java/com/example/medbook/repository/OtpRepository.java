package com.example.medbook.repository;

import com.example.medbook.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findFirstByEmailIgnoreCaseOrderByExpiresAtDesc(String email);
    void deleteByEmail(String email);
}
