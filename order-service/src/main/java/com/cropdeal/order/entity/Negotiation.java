package com.cropdeal.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cropdeal.order.enums.NegotiationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "negotiations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Negotiation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "negotiation_number", nullable = false, unique = true, length = 50)
    private String negotiationNumber;

    @Column(name = "purchase_request_id", nullable = false)
    private Long purchaseRequestId;

    @Column(name = "crop_id", nullable = false)
    private Long cropId;

    @Column(name = "farmer_id", nullable = false)
    private Long farmerId;

    @Column(name = "dealer_id", nullable = false)
    private Long dealerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NegotiationStatus status;

    @Column(name = "final_price", precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}