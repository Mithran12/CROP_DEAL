package com.cropdeal.crop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cropdeal.crop.entity.CropCategory;
import com.cropdeal.crop.entity.CropQuality;
import com.cropdeal.crop.entity.CropStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropResponse {

    private Long cropId;

    private Long farmerId;

    private String cropName;

    private CropCategory category;

    private String description;

    private Double quantity;

    private String quantityUnit;

    private BigDecimal expectedPrice;

    private CropQuality quality;

    private LocalDate harvestDate;

    private String addressLine;

    private String city;

    private String state;

    private String pincode;

    private String imageUrl;

    private CropStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}