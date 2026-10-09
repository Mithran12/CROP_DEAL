package com.cropdeal.subscription.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

public class CreateSubscriptionRequest {

    @NotNull(message = "Dealer ID is required")
    @Positive(message = "Dealer ID must be positive")
    private Long dealerId;

    @NotNull(message = "Crop ID is required")
    @Positive(message = "Crop ID must be positive")
    private Long cropId;

    @NotBlank(message = "Category is required")
    private String category;

    public Long getDealerId() {
        return dealerId;
    }

    public void setDealerId(Long dealerId) {
        this.dealerId = dealerId;
    }

    public Long getCropId() {
        return cropId;
    }

    public void setCropId(Long cropId) {
        this.cropId = cropId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}