package com.cropdeal.order.event;

import java.math.BigDecimal;

public class PaymentEvent {

    private Long orderId;

    private String paymentStatus;

    private BigDecimal amount;

    public PaymentEvent() {
    }

    public PaymentEvent(
            Long orderId,
            String paymentStatus,
            BigDecimal amount) {

        this.orderId = orderId;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}