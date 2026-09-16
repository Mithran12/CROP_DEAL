package com.cropdeal.order.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.cropdeal.order.dto.response.ErrorResponseDto;

class GlobalExceptionHandlerTest {

	@Test
	void handleExternalServiceException_shouldReturn503() {

		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		ExternalServiceException exception = new ExternalServiceException("Crop Service is currently unavailable");

		MockHttpServletRequest request = new MockHttpServletRequest();

		request.setRequestURI("/api/orders");

		ServletWebRequest webRequest = new ServletWebRequest(request);

		ResponseEntity<ErrorResponseDto> response = handler.handleExternalServiceException(exception, webRequest);

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());

		assertEquals(503, response.getBody().getStatus());

		assertEquals("Service Unavailable", response.getBody().getError());

		assertEquals("Crop Service is currently unavailable", response.getBody().getMessage());

		assertEquals("/api/orders", response.getBody().getPath());
	}
}