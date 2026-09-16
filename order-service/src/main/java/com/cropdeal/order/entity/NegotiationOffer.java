package com.cropdeal.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cropdeal.order.enums.OfferStatus;

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
@Table(name = "negotiation_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NegotiationOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "negotiation_id", nullable = false)
    private Long negotiationId;

    @Column(name = "offered_by_user_id", nullable = false)
    private Long offeredByUserId;

    @Column(name = "offered_by_role", nullable = false, length = 20)
    private String offeredByRole;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "offer_status", nullable = false, length = 20)
    private OfferStatus offerStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}