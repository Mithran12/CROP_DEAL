package com.cropdeal.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "negotiation_offers")
public class Offer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private Long negotiationId;
	@Column(nullable = false)
	private Long actorId;
	@Column(nullable = false)
	private String actorRole;
	@Column(nullable = false)
	private Double amount;
	@Column(nullable = false)
	private LocalDateTime createdAt;
	@Column(nullable = false)
	private String status;

	public Offer() {
	}

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
