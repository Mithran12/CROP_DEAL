package com.cropdeal.auth.repository;

import com.cropdeal.auth.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
	List<PasswordResetOtp> findByEmailOrderByCreatedAtDesc(String email);

	Optional<PasswordResetOtp> findTopByEmailOrderByCreatedAtDesc(String email);

	Optional<PasswordResetOtp> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);
}
