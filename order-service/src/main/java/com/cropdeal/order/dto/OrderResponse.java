package com.cropdeal.order.dto;



import java.time.LocalDateTime;

import com.cropdeal.order.entity.OrderStatus;
import com.cropdeal.order.entity.PaymentStatus;
import com.cropdeal.order.entity.PickupStatus;

public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long farmerId;
    private Long dealerId;
    private Long cropId;
    private Double quantity;
    private Double agreedPrice;
    private Double totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private PickupStatus pickupStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private Boolean deliveryConfirmed;
    private com.cropdeal.order.entity.NegotiationStatus negotiationStatus;
    private Long lastOfferId;
    private Boolean quantityReserved;

    public OrderResponse() {
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public Long getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(Long farmerId) {
		this.farmerId = farmerId;
	}

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

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Double getAgreedPrice() {
		return agreedPrice;
	}

	public void setAgreedPrice(Double agreedPrice) {
		this.agreedPrice = agreedPrice;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public PickupStatus getPickupStatus() {
		return pickupStatus;
	}

	public void setPickupStatus(PickupStatus pickupStatus) {
		this.pickupStatus = pickupStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}



	public com.cropdeal.order.entity.NegotiationStatus getNegotiationStatus(){return negotiationStatus;}
	public void setNegotiationStatus(com.cropdeal.order.entity.NegotiationStatus v){negotiationStatus=v;}
	public Long getLastOfferId(){return lastOfferId;}
	public void setLastOfferId(Long v){lastOfferId=v;}

	public Boolean getQuantityReserved(){return quantityReserved;}
	public void setQuantityReserved(Boolean v){quantityReserved=v;}

	public Boolean getDeliveryConfirmed() {
		return deliveryConfirmed;
	}

	public void setDeliveryConfirmed(Boolean deliveryConfirmed) {
		this.deliveryConfirmed = deliveryConfirmed;
	}
}
