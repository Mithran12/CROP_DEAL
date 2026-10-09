package com.cropdeal.order.dto;

import jakarta.validation.constraints.*;

public class NegotiationRequest {
	@NotNull
	private Long cropId;
	@NotNull
	@Positive
	private Double quantity;
	@NotNull
	private Long dealerId;
	@NotNull
	@Positive
	private Double amount;

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double v) {
		quantity = v;
	}

	public Long getCropId() {
		return cropId;
	}

	public void setCropId(Long v) {
		cropId = v;
	}

	public Long getDealerId() {
		return dealerId;
	}

	public void setDealerId(Long v) {
		dealerId = v;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double v) {
		amount = v;
	}
}
