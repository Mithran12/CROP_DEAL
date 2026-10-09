package com.cropdeal.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_event_store", indexes = @Index(name = "idx_order_event_aggregate", columnList = "aggregate_id"))
public class OrderEventRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "aggregate_type", nullable = false)
	private String aggregateType;
	@Column(name = "aggregate_id", nullable = false)
	private String aggregateId;
	@Column(nullable = false)
	private String eventType;
	@Column(nullable = false, length = 8000)
	private String payload;
	@Column(nullable = false)
	private LocalDateTime occurredAt;

	public OrderEventRecord() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long v) {
		id = v;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public void setAggregateType(String v) {
		aggregateType = v;
	}

	public String getAggregateId() {
		return aggregateId;
	}

	public void setAggregateId(String v) {
		aggregateId = v;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String v) {
		eventType = v;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String v) {
		payload = v;
	}

	public LocalDateTime getOccurredAt() {
		return occurredAt;
	}

	public void setOccurredAt(LocalDateTime v) {
		occurredAt = v;
	}
}
