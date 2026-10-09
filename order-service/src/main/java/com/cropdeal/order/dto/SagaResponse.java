package com.cropdeal.order.dto;

public class SagaResponse {
	private Long orderId;
	private String status, message;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long v) {
		orderId = v;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String v) {
		status = v;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String v) {
		message = v;
	}
}
