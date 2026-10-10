package com.cropdeal.order.dto;

import java.time.LocalDateTime;

public class OfferResponse {
	private Long id, negotiationId, actorId;
	private String actorRole, status;
	private Double amount;
	private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long v) {
		id = v;
	}

	public Long getNegotiationId() {
		return negotiationId;
	}

	public void setNegotiationId(Long v) {
		negotiationId = v;
	}

	public Long getActorId() {
		return actorId;
	}

	public void setActorId(Long v) {
		actorId = v;
	}

	public String getActorRole() {
		return actorRole;
	}

	public void setActorRole(String v) {
		actorRole = v;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double v) {
		amount = v;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime v) {
		createdAt = v;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String v) {
		status = v;
	}
}
