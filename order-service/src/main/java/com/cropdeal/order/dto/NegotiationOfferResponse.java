package com.cropdeal.order.dto;

import com.cropdeal.order.entity.OfferStatus;

import java.time.LocalDateTime;

public class NegotiationOfferResponse {
    private Long id;
    private Long orderId;
    private Long proposerId;
    private String proposerRole;
    private Double offeredPrice;
    private String message;
    private OfferStatus status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProposerId() { return proposerId; }
    public void setProposerId(Long proposerId) { this.proposerId = proposerId; }
    public String getProposerRole() { return proposerRole; }
    public void setProposerRole(String proposerRole) { this.proposerRole = proposerRole; }
    public Double getOfferedPrice() { return offeredPrice; }
    public void setOfferedPrice(Double offeredPrice) { this.offeredPrice = offeredPrice; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
