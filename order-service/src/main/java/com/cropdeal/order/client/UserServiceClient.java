package com.cropdeal.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cropdeal.order.config.FeignAuthInterceptor;

@FeignClient(
        name = "user-service",
        url = "http://localhost:8082",
        configuration = {
            FeignClientErrorDecoder.class,
            FeignAuthInterceptor.class
        }
)
public interface UserServiceClient {

    @GetMapping("/api/users/farmers/{farmerId}")
    Object getFarmerById(@PathVariable("farmerId") Long farmerId);
}