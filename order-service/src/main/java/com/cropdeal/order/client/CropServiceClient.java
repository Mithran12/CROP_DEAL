package com.cropdeal.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "crop-service",
        url = "http://localhost:8082",
        configuration = FeignClientErrorDecoder.class
)
public interface CropServiceClient {

    @GetMapping("/api/crops/{id}")
    Object getCropById(
            @PathVariable("id") Long id);
}