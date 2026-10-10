package com.cropdeal.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_sagas")
public class OrderSaga {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private Long orderId;
	@Column(nullable = false)
	private Long cropId;
	@Column(nullable = false)
	private Double quantity;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SagaStatus status;
	@Column(nullable = false)
	private LocalDateTime startedAt;
	private LocalDateTime updatedAt;
	@Column(length = 1000)
	private String failureReason;

	public OrderSaga() {
	}

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

	public Long getCropId() {
		return cropId;
	}

	public void setCropId(Long v) {
		cropId = v;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double v) {
		quantity = v;
	}

	public SagaStatus getStatus() {
		return status;
	}

	public void setStatus(SagaStatus v) {
		status = v;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(LocalDateTime v) {
		startedAt = v;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime v) {
		updatedAt = v;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String v) {
		failureReason = v;
	}
}
