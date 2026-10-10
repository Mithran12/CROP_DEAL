package com.cropdeal.order.dto;

public class SagaPaymentRequest {
	private Long orderId;
	private String paymentMethod;
	private String dealerCardNumber, dealerCardHolderName, dealerCardExpiry, dealerCvv, farmerCardNumber,
			farmerCardHolderName, dealerUpiId, farmerUpiId;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long v) {
		orderId = v;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String v) {
		paymentMethod = v;
	}

	public String getDealerCardNumber() {
		return dealerCardNumber;
	}

	public void setDealerCardNumber(String v) {
		dealerCardNumber = v;
	}

	public String getDealerCardHolderName() {
		return dealerCardHolderName;
	}

	public void setDealerCardHolderName(String v) {
		dealerCardHolderName = v;
	}

	public String getDealerCardExpiry() {
		return dealerCardExpiry;
	}

	public void setDealerCardExpiry(String v) {
		dealerCardExpiry = v;
	}

	public String getDealerCvv() {
		return dealerCvv;
	}

	public void setDealerCvv(String v) {
		dealerCvv = v;
	}

	public String getFarmerCardNumber() {
		return farmerCardNumber;
	}

	public void setFarmerCardNumber(String v) {
		farmerCardNumber = v;
	}

	public String getFarmerCardHolderName() {
		return farmerCardHolderName;
	}

	public void setFarmerCardHolderName(String v) {
		farmerCardHolderName = v;
	}

	public String getDealerUpiId() {
		return dealerUpiId;
	}

	public void setDealerUpiId(String v) {
		dealerUpiId = v;
	}

	public String getFarmerUpiId() {
		return farmerUpiId;
	}

	public void setFarmerUpiId(String v) {
		farmerUpiId = v;
	}
}
