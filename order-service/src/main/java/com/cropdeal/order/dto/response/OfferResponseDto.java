package com.cropdeal.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cropdeal.order.enums.OfferStatus;

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
public class OfferResponseDto {

    private Long id;

    private Long negotiationId;

    private Long offeredByUserId;

    private String offeredByRole;

    private BigDecimal price;

    private BigDecimal quantity;

    private String message;

    private OfferStatus offerStatus;

    private LocalDateTime createdAt;
}