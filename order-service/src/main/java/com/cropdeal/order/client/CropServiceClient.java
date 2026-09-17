package com.cropdeal.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cropdeal.order.config.FeignAuthInterceptor;

@FeignClient(
        name = "crop-service",
        url = "http://localhost:8083",
        configuration = {
        		        FeignClientErrorDecoder.class,
        		        FeignAuthInterceptor.class
        		}
)
public interface CropServiceClient {

    @GetMapping("/api/crops/{id}")
    Object getCropById(
            @PathVariable("id") Long id);
}