package com.cropdeal.order.client;

import org.springframework.stereotype.Service;

import com.cropdeal.order.exception.ExternalServiceException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class ExternalServiceClientService {

	private final UserServiceClient userServiceClient;
	private final CropServiceClient cropServiceClient;

	public ExternalServiceClientService(UserServiceClient userServiceClient, CropServiceClient cropServiceClient) {

		this.userServiceClient = userServiceClient;
		this.cropServiceClient = cropServiceClient;
	}

	@CircuitBreaker(name = "userService", fallbackMethod = "userServiceFallback")
	public Object getUserById(Long userId) {

		return userServiceClient.getUserById(userId);
	}

	@CircuitBreaker(name = "cropService", fallbackMethod = "cropServiceFallback")
	public Object getCropById(Long cropId) {

		return cropServiceClient.getCropById(cropId);
	}

	private Object userServiceFallback(Long userId, Throwable throwable) {

		throw new ExternalServiceException("User Service is currently unavailable", throwable);
	}

	private Object cropServiceFallback(Long cropId, Throwable throwable) {

		throw new ExternalServiceException("Crop Service is currently unavailable", throwable);
	}
}