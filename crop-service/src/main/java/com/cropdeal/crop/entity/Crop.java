package com.cropdeal.crop.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "crops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cropId;
    private Long farmerId;

    @Column(nullable = false)
    private String cropName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropCategory category;

    private String description;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private String quantityUnit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal expectedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropQuality quality;

    private LocalDate harvestDate;

    private String addressLine;

    private String city;

    private String state;

    private String pincode;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CropStatus status = CropStatus.UNPUBLISHED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}