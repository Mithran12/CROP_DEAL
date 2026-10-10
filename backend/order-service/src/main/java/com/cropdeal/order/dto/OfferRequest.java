package com.cropdeal.order.dto;

import jakarta.validation.constraints.*;

public class OfferRequest {
	@NotNull
	@Positive
	private Double amount;

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double v) {
		amount = v;
	}
}
