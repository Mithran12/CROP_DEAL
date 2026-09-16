package com.cropdeal.order.client;

import com.cropdeal.order.exception.ExternalServiceException;

import org.springframework.stereotype.Component;

@Component
public class ExternalServiceFallback {

    public ExternalServiceException userServiceUnavailable() {
        return new ExternalServiceException(
                "User Service is currently unavailable");
    }

    public ExternalServiceException cropServiceUnavailable() {
        return new ExternalServiceException(
                "Crop Service is currently unavailable");
    }
}