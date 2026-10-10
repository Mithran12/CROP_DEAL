package com.cropdeal.order.dto;

import com.cropdeal.order.entity.NegotiationStatus;
import java.time.LocalDateTime;
import java.util.List;

public class NegotiationResponse {
	private Long id, cropId, farmerId, dealerId, orderId;
	private String orderNumber;
	private String nextAction;
	private Double quantity, askingPrice, currentOffer;
	private NegotiationStatus status;
	private LocalDateTime createdAt, updatedAt, acceptedAt;
	private List<OfferResponse> offers;
	private String cropName, farmerName, dealerName;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long v) {
		orderId = v;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String v) {
		orderNumber = v;
	}

	public String getCropName() {
		return cropName;
	}

	public void setCropName(String v) {
		cropName = v;
	}

	public String getFarmerName() {
		return farmerName;
	}

	public void setFarmerName(String v) {
		farmerName = v;
	}

	public String getDealerName() {
		return dealerName;
	}

	public void setDealerName(String v) {
		dealerName = v;
	}

	public String getNextAction() {
		return nextAction;
	}

	public void setNextAction(String v) {
		nextAction = v;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long v) {
		id = v;
	}

	public Long getCropId() {
		return cropId;
	}

	public void setCropId(Long v) {
		cropId = v;
	}

	public Long getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(Long v) {
		farmerId = v;
	}

	public Long getDealerId() {
		return dealerId;
	}

	public void setDealerId(Long v) {
		dealerId = v;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double v) {
		quantity = v;
	}

	public Double getAskingPrice() {
		return askingPrice;
	}

	public void setAskingPrice(Double v) {
		askingPrice = v;
	}

	public Double getCurrentOffer() {
		return currentOffer;
	}

	public void setCurrentOffer(Double v) {
		currentOffer = v;
	}

	public NegotiationStatus getStatus() {
		return status;
	}

	public void setStatus(NegotiationStatus v) {
		status = v;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime v) {
		createdAt = v;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime v) {
		updatedAt = v;
	}

	public LocalDateTime getAcceptedAt() {
		return acceptedAt;
	}

	public void setAcceptedAt(LocalDateTime v) {
		acceptedAt = v;
	}

	public List<OfferResponse> getOffers() {
		return offers;
	}

	public void setOffers(List<OfferResponse> v) {
		offers = v;
	}
}
