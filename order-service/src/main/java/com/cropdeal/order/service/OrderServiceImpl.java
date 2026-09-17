package com.cropdeal.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cropdeal.order.client.ExternalServiceClientService;
import com.cropdeal.order.dto.request.CreateOrderRequestDto;
import com.cropdeal.order.dto.request.UpdateOrderStatusRequestDto;
import com.cropdeal.order.dto.response.OrderResponseDto;
import com.cropdeal.order.entity.Order;
import com.cropdeal.order.entity.OrderStatusHistory;
import com.cropdeal.order.enums.OrderStatus;
import com.cropdeal.order.enums.PaymentStatus;
import com.cropdeal.order.enums.PickupStatus;
import com.cropdeal.order.exception.BusinessException;
import com.cropdeal.order.exception.ResourceNotFoundException;
import com.cropdeal.order.exception.UnauthorizedResourceException;
import com.cropdeal.order.repository.OrderRepository;
import com.cropdeal.order.repository.OrderStatusHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ExternalServiceClientService externalServiceClientService;

    // 1. CREATE ORDER

    @Override
    @Transactional
    public OrderResponseDto createOrder(
            CreateOrderRequestDto requestDto,
            Long dealerId) {

        log.info(
                "Creating order for dealerId={}, farmerId={}, cropId={}",
                dealerId,
                requestDto.getFarmerId(),
                requestDto.getCropId());

        // Verify crop exists in Crop Service
        Object cropResponse =
                externalServiceClientService.getCropById(
                        requestDto.getCropId());

        // Verify that farmer belongs to the selected crop
        if (cropResponse instanceof Map<?, ?> cropData) {

            Object cropFarmerId = cropData.get("farmerId");

            if (!(cropFarmerId instanceof Number)) {
                throw new BusinessException(
                        "Invalid farmer ID received from Crop Service");
            }

            Long actualFarmerId =
                    ((Number) cropFarmerId).longValue();

            if (!actualFarmerId.equals(requestDto.getFarmerId())) {
                throw new BusinessException(
                        "Farmer does not belong to the selected crop");
            }

        } else {
            throw new BusinessException(
                    "Invalid response received from Crop Service");
        }

        Order order = new Order();

        order.setOrderNumber(
                "ORD-" + System.currentTimeMillis());

        order.setFarmerId(requestDto.getFarmerId());
        order.setDealerId(dealerId);
        order.setCropId(requestDto.getCropId());

        order.setQuantity(requestDto.getQuantity());
        order.setAgreedPrice(requestDto.getAgreedPrice());

        BigDecimal totalAmount =
                requestDto.getQuantity()
                        .multiply(requestDto.getAgreedPrice());

        order.setTotalAmount(totalAmount);

        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPickupStatus(PickupStatus.PENDING);

        LocalDateTime now = LocalDateTime.now();

        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        Order savedOrder =
                orderRepository.save(order);

        saveStatusHistory(
                savedOrder.getId(),
                null,
                OrderStatus.PAYMENT_PENDING,
                dealerId,
                "Order created");

        log.info(
                "Order created successfully. orderId={}, dealerId={}, farmerId={}",
                savedOrder.getId(),
                dealerId,
                requestDto.getFarmerId());

        return mapToResponseDto(savedOrder);
    }

    // 2. GET ORDER BY ID

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(
            Long id,
            Long authenticatedUserId) {

        Order order =
                findOrderById(id);

        validateParticipantAccess(
                order,
                authenticatedUserId);

        return mapToResponseDto(order);
    }

    // 3. GET ORDERS BY FARMER

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByFarmer(
            Long farmerId,
            Long authenticatedUserId) {

        validateUserId(
                farmerId,
                authenticatedUserId,
                "You are not authorized to access these farmer orders");

        return orderRepository
                .findByFarmerId(farmerId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    // 4. GET ORDERS BY DEALER

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByDealer(
            Long dealerId,
            Long authenticatedUserId) {

        validateUserId(
                dealerId,
                authenticatedUserId,
                "You are not authorized to access these dealer orders");

        return orderRepository
                .findByDealerId(dealerId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    // 5. UPDATE ORDER STATUS

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(
            Long id,
            UpdateOrderStatusRequestDto requestDto,
            Long changedBy,
            String role) {

        Order order =
                findOrderById(id);

        // Only farmer or dealer associated with this order
        // can update its status.
        validateParticipantAccess(
                order,
                changedBy);

        // Get current and requested status
        OrderStatus oldStatus =
                order.getStatus();

        OrderStatus newStatus =
                requestDto.getStatus();

     // Validate whether the transition is part of the lifecycle
        validateStatusTransition(
                oldStatus,
                newStatus);

        // Validate whether this role can perform the transition
        validateRoleBasedTransition(
                order,
                oldStatus,
                newStatus,
                changedBy,
                role);

        order.setStatus(newStatus);

        updatePaymentAndPickupStatus(
                order,
                newStatus);

        order.setUpdatedAt(
                LocalDateTime.now());

        if (newStatus == OrderStatus.COMPLETED) {
            order.setCompletedAt(
                    LocalDateTime.now());
        }

        Order updatedOrder =
                orderRepository.save(order);

        saveStatusHistory(
                updatedOrder.getId(),
                oldStatus,
                newStatus,
                changedBy,
                requestDto.getReason());

        log.info(
                "Order status updated. orderId={}, oldStatus={}, newStatus={}, changedBy={}",
                id,
                oldStatus,
                newStatus,
                changedBy);

        return mapToResponseDto(updatedOrder);
    }

    // 6. CANCEL ORDER

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(
            Long id,
            Long changedBy) {

        Order order =
                findOrderById(id);

        // Cancel is a dealer operation.
        validateDealerAccess(
                order,
                changedBy);

        OrderStatus oldStatus =
                order.getStatus();

        if (oldStatus == OrderStatus.COMPLETED) {
            throw new BusinessException(
                    "Completed order cannot be cancelled");
        }

        if (oldStatus == OrderStatus.CANCELLED) {
            throw new BusinessException(
                    "Order is already cancelled");
        }

        order.setStatus(
                OrderStatus.CANCELLED);

        order.setUpdatedAt(
                LocalDateTime.now());

        Order updatedOrder =
                orderRepository.save(order);

        saveStatusHistory(
                updatedOrder.getId(),
                oldStatus,
                OrderStatus.CANCELLED,
                changedBy,
                "Order cancelled");

        log.info(
                "Order cancelled. orderId={}, dealerId={}",
                id,
                changedBy);

        return mapToResponseDto(updatedOrder);
    }

    // 7. COMPLETE ORDER

    @Override
    @Transactional
    public OrderResponseDto completeOrder(
            Long id,
            Long changedBy) {

        Order order =
                findOrderById(id);

        // Complete is a dealer operation.
        validateDealerAccess(
                order,
                changedBy);

        OrderStatus oldStatus =
                order.getStatus();

        if (oldStatus != OrderStatus.PICKED_UP) {
            throw new BusinessException(
                    "Only picked-up orders can be completed");
        }

        order.setStatus(
                OrderStatus.COMPLETED);

        order.setCompletedAt(
                LocalDateTime.now());

        order.setUpdatedAt(
                LocalDateTime.now());

        Order updatedOrder =
                orderRepository.save(order);

        saveStatusHistory(
                updatedOrder.getId(),
                oldStatus,
                OrderStatus.COMPLETED,
                changedBy,
                "Order completed");

        log.info(
                "Order completed. orderId={}, dealerId={}",
                id,
                changedBy);

        return mapToResponseDto(updatedOrder);
    }

    // 8. HANDLE PAYMENT EVENT

    @Override
    @Transactional
    public OrderResponseDto handlePaymentEvent(
            Long orderId,
            PaymentStatus paymentStatus,
            BigDecimal amount) {

        Order order =
                findOrderById(orderId);

        if (paymentStatus == null) {
            throw new BusinessException(
                    "Payment status cannot be null");
        }

        if (amount == null) {
            throw new BusinessException(
                    "Payment amount cannot be null");
        }

        if (order.getTotalAmount().compareTo(amount) != 0) {
            throw new BusinessException(
                    "Payment amount does not match order total amount");
        }

        // Ignore duplicate payment events
        if (order.getPaymentStatus() == paymentStatus) {
            log.info(
                    "Duplicate payment event ignored. orderId={}, paymentStatus={}",
                    orderId,
                    paymentStatus);

            return mapToResponseDto(order);
        }

        OrderStatus oldStatus =
                order.getStatus();

        // PAID

        if (paymentStatus == PaymentStatus.PAID) {

            if (oldStatus != OrderStatus.PAYMENT_PENDING) {
                throw new BusinessException(
                        "Payment cannot be marked as PAID for order status: "
                                + oldStatus);
            }

            order.setPaymentStatus(
                    PaymentStatus.PAID);

            order.setStatus(
                    OrderStatus.PAID);

            order.setUpdatedAt(
                    LocalDateTime.now());

            Order updatedOrder =
                    orderRepository.save(order);

            saveStatusHistory(
                    updatedOrder.getId(),
                    oldStatus,
                    OrderStatus.PAID,
                    0L,
                    "Payment successful");

            log.info(
                    "Payment marked PAID. orderId={}",
                    orderId);

            return mapToResponseDto(updatedOrder);
        }

        // FAILED

        if (paymentStatus == PaymentStatus.FAILED) {

            if (oldStatus != OrderStatus.PAYMENT_PENDING) {
                throw new BusinessException(
                        "Payment cannot be marked as FAILED for order status: "
                                + oldStatus);
            }

            order.setPaymentStatus(
                    PaymentStatus.FAILED);

            order.setStatus(
                    OrderStatus.PAYMENT_FAILED);

            order.setUpdatedAt(
                    LocalDateTime.now());

            Order updatedOrder =
                    orderRepository.save(order);

            saveStatusHistory(
                    updatedOrder.getId(),
                    oldStatus,
                    OrderStatus.PAYMENT_FAILED,
                    0L,
                    "Payment failed");

            log.info(
                    "Payment marked FAILED. orderId={}",
                    orderId);

            return mapToResponseDto(updatedOrder);
        }

        // REFUNDED

        if (paymentStatus == PaymentStatus.REFUNDED) {

            order.setPaymentStatus(
                    PaymentStatus.REFUNDED);

            if (oldStatus != OrderStatus.CANCELLED) {

                order.setStatus(
                        OrderStatus.CANCELLED);

                order.setUpdatedAt(
                        LocalDateTime.now());

                Order updatedOrder =
                        orderRepository.save(order);

                saveStatusHistory(
                        updatedOrder.getId(),
                        oldStatus,
                        OrderStatus.CANCELLED,
                        0L,
                        "Payment refunded");

                log.info(
                        "Payment refunded and order cancelled. orderId={}",
                        orderId);

                return mapToResponseDto(updatedOrder);
            }

            order.setUpdatedAt(
                    LocalDateTime.now());

            Order updatedOrder =
                    orderRepository.save(order);

            return mapToResponseDto(updatedOrder);
        }

        throw new BusinessException(
                "Unsupported payment status: "
                        + paymentStatus);
    }

    // STATUS TRANSITION VALIDATION   

    private void validateStatusTransition(
            OrderStatus oldStatus,
            OrderStatus newStatus) {

        if (oldStatus == null || newStatus == null) {
            throw new BusinessException(
                    "Order status cannot be null");
        }

        if (oldStatus == newStatus) {
            throw new BusinessException(
                    "Order is already in status: "
                            + oldStatus);
        }

        boolean validTransition =
                (oldStatus == OrderStatus.PAYMENT_PENDING
                        && newStatus == OrderStatus.PAID)

                || (oldStatus == OrderStatus.PAYMENT_PENDING
                        && newStatus == OrderStatus.PAYMENT_FAILED)

                || (oldStatus == OrderStatus.PAID
                        && newStatus == OrderStatus.READY_FOR_PICKUP)

                || (oldStatus == OrderStatus.READY_FOR_PICKUP
                        && newStatus == OrderStatus.PICKED_UP)

                || (oldStatus == OrderStatus.PICKED_UP
                        && newStatus == OrderStatus.COMPLETED);

        if (!validTransition) {
            throw new BusinessException(
                    "Invalid order status transition from "
                            + oldStatus
                            + " to "
                            + newStatus);
        }
    }

    // PAYMENT / PICKUP STATUS UPDATE

    private void updatePaymentAndPickupStatus(
            Order order,
            OrderStatus newStatus) {

        if (newStatus == OrderStatus.PAID) {

            order.setPaymentStatus(
                    PaymentStatus.PAID);

        } else if (newStatus == OrderStatus.PAYMENT_FAILED) {

            order.setPaymentStatus(
                    PaymentStatus.FAILED);

        } else if (newStatus == OrderStatus.READY_FOR_PICKUP) {

            order.setPickupStatus(
                    PickupStatus.READY_FOR_PICKUP);

        } else if (newStatus == OrderStatus.PICKED_UP) {

            order.setPickupStatus(
                    PickupStatus.PICKED_UP);
        }
    }
    
 // ROLE-BASED STATUS TRANSITION VALIDATION

    private void validateRoleBasedTransition(
            Order order,
            OrderStatus oldStatus,
            OrderStatus newStatus,
            Long changedBy,
            String role) {

        if (role == null || role.isBlank()) {
            throw new UnauthorizedResourceException(
                    "User role is missing");
        }

        boolean isFarmer =
                changedBy != null
                        && changedBy.equals(order.getFarmerId());

        boolean isDealer =
                changedBy != null
                        && changedBy.equals(order.getDealerId());

        if ("FARMER".equalsIgnoreCase(role)) {

            if (!isFarmer) {
                throw new UnauthorizedResourceException(
                        "Only the associated farmer can perform this action");
            }

            if (oldStatus == OrderStatus.PAID
                    && newStatus == OrderStatus.READY_FOR_PICKUP) {
                return;
            }

            throw new UnauthorizedResourceException(
                    "Farmer is not authorized for this status transition");
        }

        if ("DEALER".equalsIgnoreCase(role)) {

            if (!isDealer) {
                throw new UnauthorizedResourceException(
                        "Only the associated dealer can perform this action");
            }

            if (oldStatus == OrderStatus.READY_FOR_PICKUP
                    && newStatus == OrderStatus.PICKED_UP) {
                return;
            }

            throw new UnauthorizedResourceException(
                    "Dealer is not authorized for this status transition");
        }

        throw new UnauthorizedResourceException(
                "Invalid user role");
    }

    // AUTHORIZATION

    private void validateParticipantAccess(
            Order order,
            Long authenticatedUserId) {

        boolean isFarmer =
                authenticatedUserId != null
                        && authenticatedUserId.equals(
                                order.getFarmerId());

        boolean isDealer =
                authenticatedUserId != null
                        && authenticatedUserId.equals(
                                order.getDealerId());

        if (!isFarmer && !isDealer) {
            throw new UnauthorizedResourceException(
                    "You are not authorized to access this order");
        }
    }

    private void validateDealerAccess(
            Order order,
            Long authenticatedUserId) {

        if (authenticatedUserId == null
                || !authenticatedUserId.equals(order.getDealerId())) {

            throw new UnauthorizedResourceException(
                    "You are not authorized to modify this order");
        }
    }

    private void validateUserId(
            Long resourceUserId,
            Long authenticatedUserId,
            String message) {

        if (resourceUserId == null
                || authenticatedUserId == null
                || !resourceUserId.equals(authenticatedUserId)) {

            throw new UnauthorizedResourceException(
                    message);
        }
    }

    // FIND ORDER

    private Order findOrderById(Long id) {

        return orderRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id: "
                                        + id));
    }

    // SAVE ORDER STATUS HISTORY

    private void saveStatusHistory(
            Long orderId,
            OrderStatus oldStatus,
            OrderStatus newStatus,
            Long changedBy,
            String reason) {

        OrderStatusHistory history =
                new OrderStatusHistory();

        history.setOrderId(orderId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setReason(reason);
        history.setTimestamp(LocalDateTime.now());

        orderStatusHistoryRepository.save(history);
    }

    // ENTITY -> RESPONSE DTO

    private OrderResponseDto mapToResponseDto(
            Order order) {

        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .farmerId(order.getFarmerId())
                .dealerId(order.getDealerId())
                .cropId(order.getCropId())
                .quantity(order.getQuantity())
                .agreedPrice(order.getAgreedPrice())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .pickupStatus(order.getPickupStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }
}