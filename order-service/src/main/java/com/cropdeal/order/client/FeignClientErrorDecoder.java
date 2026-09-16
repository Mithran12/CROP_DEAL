package com.cropdeal.order.client;

import com.cropdeal.order.exception.ExternalServiceException;

import feign.Response;
import feign.codec.ErrorDecoder;

import org.springframework.stereotype.Component;

@Component
public class FeignClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(
            String methodKey,
            Response response) {

        return new ExternalServiceException(
                "External service call failed: "
                        + methodKey
                        + " | HTTP status: "
                        + response.status());
    }
}