package com.cropdeal.auction.dto;

import java.time.LocalDateTime;
import com.cropdeal.auction.entity.AuctionStatus;

public class AuctionResponse {

	private Long id;
	private Long cropId;
	private Long farmerId;
	private Double basePrice;
	private Double highestBid;
	private Long highestBidderId;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private AuctionStatus status;

	private String cropName;
	private String farmerName;
	private String winnerName;
	private String winningDealer;
	private String location;
	private Double quantity;
	private Integer totalBids;
	private Long orderId;
	private String orderNumber;
	private String paymentStatus;
	private String deliveryStatus;
	private LocalDateTime completedAt;

	public AuctionResponse() {
	}

	public String getWinningDealer() {
		return winningDealer != null ? winningDealer : winnerName;
	}

	public void setWinningDealer(String winningDealer) {
		this.winningDealer = winningDealer;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCropId() {
		return cropId;
	}

	public void setCropId(Long cropId) {
		this.cropId = cropId;
	}

	public Long getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(Long farmerId) {
		this.farmerId = farmerId;
	}

	public Double getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(Double basePrice) {
		this.basePrice = basePrice;
	}

	public Double getHighestBid() {
		return highestBid;
	}

	public void setHighestBid(Double highestBid) {
		this.highestBid = highestBid;
	}

	public Long getHighestBidderId() {
		return highestBidderId;
	}

	public void setHighestBidderId(Long highestBidderId) {
		this.highestBidderId = highestBidderId;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public AuctionStatus getStatus() {
		return status;
	}

	public void setStatus(AuctionStatus status) {
		this.status = status;
	}

	public String getCropName() {
		return cropName;
	}

	public void setCropName(String cropName) {
		this.cropName = cropName;
	}

	public String getFarmerName() {
		return farmerName;
	}

	public void setFarmerName(String farmerName) {
		this.farmerName = farmerName;
	}

	public String getWinnerName() {
		return winnerName;
	}

	public void setWinnerName(String winnerName) {
		this.winnerName = winnerName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Integer getTotalBids() {
		return totalBids;
	}

	public void setTotalBids(Integer totalBids) {
		this.totalBids = totalBids;
	}
}