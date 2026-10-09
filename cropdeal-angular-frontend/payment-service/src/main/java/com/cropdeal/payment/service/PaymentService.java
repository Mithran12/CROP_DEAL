package com.cropdeal.payment.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cropdeal.payment.dto.request.CreatePaymentRequest;
import com.cropdeal.payment.dto.request.ProcessPaymentRequest;
import com.cropdeal.payment.dto.response.PaymentEventResponse;
import com.cropdeal.payment.dto.response.PaymentResponse;

@Service
public interface PaymentService {

	PaymentResponse createPayment (CreatePaymentRequest request);
	PaymentResponse getPaymentById(Long paymentId);
	PaymentResponse getPaymentByOrderId(Long orderId);
	PaymentResponse getPaymentStatus(Long paymentId);
	PaymentResponse processPayment(Long paymentId,ProcessPaymentRequest request);
	List<PaymentEventResponse> getPaymentEvents(Long paymentId);
	
}
