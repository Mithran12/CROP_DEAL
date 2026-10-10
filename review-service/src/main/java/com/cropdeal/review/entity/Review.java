package com.cropdeal.review.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(name = "uk_review_order_reviewer", columnNames = {
		"order_id", "reviewer_id", "review_type" }))
public class Review {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "order_id", nullable = false)
	private Long orderId;
	@Column(name = "reviewer_id", nullable = false)
	private Long reviewerId;
	@Column(name = "target_user_id", nullable = false)
	private Long targetUserId;
	@Enumerated(EnumType.STRING)
	@Column(name = "review_type", nullable = false)
	private ReviewType reviewType;
	@Column(nullable = false)
	private Integer rating;
	@Column(length = 2000)
	private String review;
	@Column(nullable = false)
	private LocalDateTime createdAt;

	public Review() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long v) {
		id = v;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long v) {
		orderId = v;
	}

	public Long getReviewerId() {
		return reviewerId;
	}

	public void setReviewerId(Long v) {
		reviewerId = v;
	}

	public Long getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(Long v) {
		targetUserId = v;
	}

	public ReviewType getReviewType() {
		return reviewType;
	}

	public void setReviewType(ReviewType v) {
		reviewType = v;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer v) {
		rating = v;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String v) {
		review = v;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime v) {
		createdAt = v;
	}
}
