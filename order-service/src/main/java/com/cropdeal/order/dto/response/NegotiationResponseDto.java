package com.cropdeal.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cropdeal.order.enums.NegotiationStatus;

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
public class NegotiationResponseDto {

    private Long id;

    private String negotiationNumber;

    private Long purchaseRequestId;

    private Long cropId;

    private Long farmerId;

    private Long dealerId;

    private NegotiationStatus status;

    private BigDecimal finalPrice;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}