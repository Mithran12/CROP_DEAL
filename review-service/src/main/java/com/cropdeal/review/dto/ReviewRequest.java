package com.cropdeal.review.dto;

import jakarta.validation.constraints.*;

public class ReviewRequest {
	@NotNull
	private Long orderId;
	@NotNull
	@Min(1)
	@Max(5)
	private Integer rating;
	@Size(max = 2000)
	private String review;

	public ReviewRequest() {
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long v) {
		orderId = v;
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
}
