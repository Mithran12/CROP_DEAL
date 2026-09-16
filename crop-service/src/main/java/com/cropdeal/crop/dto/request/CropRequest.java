package com.cropdeal.crop.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.cropdeal.crop.entity.CropCategory;
import com.cropdeal.crop.entity.CropQuality;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
public class CropRequest {

    @NotNull(message = "Farmer ID is required")
    @Positive(message = "Farmer ID must be positive")
    private Long farmerId;

    @NotBlank(message = "Crop name is required")
    private String cropName;

    @NotNull(message = "Category is required")
    private CropCategory category;

    private String description;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Double quantity;

    @NotBlank(message = "Quantity unit is required")
    private String quantityUnit;

    @NotNull(message = "Expected price is required")
    @DecimalMin(value = "0.01", message = "Expected price must be greater than zero")
    private BigDecimal expectedPrice;

    @NotNull(message = "Quality is required")
    private CropQuality quality;

    @FutureOrPresent(message = "Harvest date cannot be in the past")
    private LocalDate harvestDate;

    private String addressLine;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    private String imageUrl;
}