package com.cropdeal.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "negotiations")
public class Negotiation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private Long cropId;
	@Column(nullable = false)
	private Long farmerId;
	@Column(nullable = false)
	private Long dealerId;
	@Column(nullable = false)
	private Double quantity;
	@Column(nullable = false)
	private Double askingPrice;
	@Column(nullable = false)
	private Double currentOffer;
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private NegotiationStatus status;
	@Column(nullable = false)
	private LocalDateTime createdAt;
	@Column(nullable = false)
	private LocalDateTime updatedAt;
	private LocalDateTime acceptedAt;
	private Long orderId;
	private String orderNumber;

	public Negotiation() {
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long v) {
		orderId = v;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String v) {
		orderNumber = v;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long v) {
		id = v;
	}

	public Long getCropId() {
		return cropId;
	}

	public void setCropId(Long v) {
		cropId = v;
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

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double v) {
		quantity = v;
	}

	public Double getAskingPrice() {
		return askingPrice;
	}

	public void setAskingPrice(Double v) {
		askingPrice = v;
	}

	public Double getCurrentOffer() {
		return currentOffer;
	}

	public void setCurrentOffer(Double v) {
		currentOffer = v;
	}

	public NegotiationStatus getStatus() {
		return status;
	}

	public void setStatus(NegotiationStatus v) {
		status = v;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime v) {
		createdAt = v;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime v) {
		updatedAt = v;
	}

	public LocalDateTime getAcceptedAt() {
		return acceptedAt;
	}

	public void setAcceptedAt(LocalDateTime v) {
		acceptedAt = v;
	}
}
