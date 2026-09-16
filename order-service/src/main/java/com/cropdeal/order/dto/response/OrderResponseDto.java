package com.cropdeal.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cropdeal.order.enums.OrderStatus;
import com.cropdeal.order.enums.PaymentStatus;
import com.cropdeal.order.enums.PickupStatus;

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
public class OrderResponseDto {

    private Long id;

    private String orderNumber;

    private Long farmerId;

    private Long dealerId;

    private Long cropId;

    private BigDecimal quantity;

    private BigDecimal agreedPrice;

    private BigDecimal totalAmount;

    private OrderStatus status;

    private PaymentStatus paymentStatus;

    private PickupStatus pickupStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;
}