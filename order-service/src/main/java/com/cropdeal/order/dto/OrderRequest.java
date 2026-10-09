package com.cropdeal.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrderRequest {

	@NotNull(message = "Dealer id is required")
	private Long dealerId;

	@NotNull(message = "Crop id is required")
	private Long cropId;

	@NotNull(message = "Quantity is required")
	@Positive(message = "Quantity must be greater than 0")
	private Double quantity;

	@NotNull(message = "Agreed price is required")
	@Positive(message = "Agreed price must be greater than 0")
	private Double agreedPrice;

	private Long farmerId;
	private Long auctionId;

	public OrderRequest() {
	}

	public Long getAuctionId() {
		return auctionId;
	}

	public void setAuctionId(Long auctionId) {
		this.auctionId = auctionId;
	}

	public Long getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(Long farmerId) {
		this.farmerId = farmerId;
	}

	public Long getDealerId() {
		return dealerId;
	}

	public void setDealerId(Long dealerId) {
		this.dealerId = dealerId;
	}

	public Long getCropId() {
		return cropId;
	}

	public void setCropId(Long cropId) {
		this.cropId = cropId;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Double getAgreedPrice() {
		return agreedPrice;
	}

	public void setAgreedPrice(Double agreedPrice) {
		this.agreedPrice = agreedPrice;
	}

}