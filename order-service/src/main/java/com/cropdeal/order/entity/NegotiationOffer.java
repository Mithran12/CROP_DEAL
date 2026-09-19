package com.cropdeal.order.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "negotiation_offers")
public class NegotiationOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long proposerId;

    @Column(nullable = false)
    private String proposerRole;

    @Column(nullable = false)
    private Double offeredPrice;

    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    @Column(nullable = false)
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
