package com.cropdeal.review.dto;

public class OrderResponse {
	private Long id, farmerId, dealerId;
	private String status;

	public Long getId() {
		return id;
	}

	public void setId(Long v) {
		id = v;
	}

	public Long getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(Long v) {
		farmerId = v;
	}

	public Long getDealerId() {
		return dealerId;
	}

	public void setDealerId(Long v) {
		dealerId = v;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String v) {
		status = v;
	}
}
