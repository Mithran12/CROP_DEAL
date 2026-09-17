package com.cropdeal.order.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
public class CreatePurchaseRequestDto {

    @NotNull(message = "Crop ID is required")
    @Positive(message = "Crop ID must be positive")
    private Long cropId;

    @NotNull(message = "Requested quantity is required")
    @DecimalMin(value = "0.001", message = "Requested quantity must be greater than zero")
    private BigDecimal requestedQuantity;

    @NotNull(message = "Offered price is required")
    @DecimalMin(value = "0.01", message = "Offered price must be greater than zero")
    private BigDecimal offeredPrice;

    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;
}