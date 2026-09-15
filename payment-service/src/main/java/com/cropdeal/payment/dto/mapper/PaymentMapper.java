package com.cropdeal.payment.dto.mapper;

import org.springframework.stereotype.Component;

import com.cropdeal.payment.dto.request.CreatePaymentRequest;
import com.cropdeal.payment.dto.response.PaymentEventResponse;
import com.cropdeal.payment.dto.response.PaymentResponse;
import com.cropdeal.payment.entity.Payment;
import com.cropdeal.payment.entity.PaymentEvent;

@Component
public class PaymentMapper {

	public Payment toEntity(CreatePaymentRequest request) {
		
		Payment payment = new Payment();
		
		payment.setOrderId(request.getOrderId());
		payment.setDealerId(request.getDealerId());
		payment.setFarmerId(request.getFarmerId());
		payment.setAmount(request.getAmount());
		payment.setPaymentMethod(request.getPaymentMethod());
		
		return payment;
	}
	
	public PaymentResponse toresponse(Payment payment) {
		
		PaymentResponse response = new PaymentResponse();
		
		response.setPaymentId(payment.getPaymentId());
		response.setOrderId(payment.getOrderId());
		response.setDealerId(payment.getDealerId());
		response.setFarmerId(payment.getFarmerId());
		response.setAmount(payment.getAmount());
		response.setCurrency(payment.getCurrency());
		response.setPaymentMethod(payment.getPaymentMethod());
		response.setStatus(payment.getStatus());
		response.setTransactionReference(payment.getTransactionReference());
		response.setCreatedAt(payment.getCreatedAt());
		response.setUpdatedAt(payment.getUpdatedAt());
		
		return response;
		
	}
	
	public PaymentEventResponse toeventresponse(PaymentEvent event) {
		
		PaymentEventResponse response = new PaymentEventResponse();
		
		response.setEventId(event.getEventId());
		response.setPaymentId(event.getPaymentId());
		response.setOrderId(event.getOrderId());
		response.setEventType(event.getEventType());
		response.setAmount(event.getAmount());
		response.setMetadata(event.getMetadata());
		response.setCreatedAt(event.getCreatedAt());
		
		return response;
	}
}
