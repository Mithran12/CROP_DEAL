package com.cropdeal.auth.repository;

import com.cropdeal.auth.entity.RegistrationOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RegistrationOtpRepository extends JpaRepository<RegistrationOtp, Long> {
	Optional<RegistrationOtp> findTopByEmailOrderByCreatedAtDesc(String email);

	Optional<RegistrationOtp> findTopByEmailAndVerifiedTrueOrderByCreatedAtDesc(String email);
}
