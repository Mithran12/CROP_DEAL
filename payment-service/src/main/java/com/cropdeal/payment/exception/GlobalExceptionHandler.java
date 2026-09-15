package com.cropdeal.payment.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cropdeal.payment.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(PaymentNotFoundException.class)
	public ResponseEntity <ErrorResponse> handlePaymentNotFound(
			PaymentNotFoundException ex){
		
		ErrorResponse errorResponse = new ErrorResponse(
				ex.getMessage(),
				"PAYMENT_NOT_FOUND",
				LocalDateTime.now());
		
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(errorResponse);
	}
	
	@ExceptionHandler(DuplicatePaymentException.class)
	public ResponseEntity<ErrorResponse> handleDuplicatePayment(
			DuplicatePaymentException ex){
		
		ErrorResponse errorResponse = new ErrorResponse(
				ex.getMessage(),
				"DUPLICATE_PAYMENT",
				LocalDateTime.now());
		
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(errorResponse);
	}
	
	@ExceptionHandler(InvalidPaymentStateException.class)
	public ResponseEntity<ErrorResponse> handleInvalidPaymentState(
			InvalidPaymentStateException ex){
		
		ErrorResponse errorResponse = new ErrorResponse(
				)
	}
}
