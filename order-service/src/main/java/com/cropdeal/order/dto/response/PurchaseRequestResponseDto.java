package com.cropdeal.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cropdeal.order.enums.PurchaseRequestStatus;

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
public class PurchaseRequestResponseDto {

    private Long id;

    private String requestNumber;

    private Long cropId;

    private Long farmerId;

    private Long dealerId;

    private BigDecimal requestedQuantity;

    private BigDecimal offeredPrice;

    private String message;

    private PurchaseRequestStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}