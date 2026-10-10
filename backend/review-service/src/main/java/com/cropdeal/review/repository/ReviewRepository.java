package com.cropdeal.review.repository;

import com.cropdeal.review.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	Optional<Review> findByOrderIdAndReviewerIdAndReviewType(Long orderId, Long reviewerId, ReviewType type);

	List<Review> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);
}
