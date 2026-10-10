package com.cropdeal.review.dto;

import com.cropdeal.review.entity.ReviewType;
import java.time.LocalDateTime;

public class ReviewResponse {
	private Long id, orderId, reviewerId, targetUserId;
	private ReviewType reviewType;
	private Integer rating;
	private String review;
	private LocalDateTime createdAt;

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
