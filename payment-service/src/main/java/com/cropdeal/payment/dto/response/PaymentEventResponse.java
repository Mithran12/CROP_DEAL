package com.cropdeal.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentEventResponse {

    private Long eventId;
    private Long paymentId;
    private Long orderId;
    private String eventType;
    private BigDecimal amount;
    private String metadata;
    private LocalDateTime createdAt;

    public PaymentEventResponse() {
    }

    public PaymentEventResponse(
            Long eventId,
            Long paymentId,
            Long orderId,
            String eventType,
            BigDecimal amount,
            String metadata,
            LocalDateTime createdAt
    ) {
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.eventType = eventType;
        this.amount = amount;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}