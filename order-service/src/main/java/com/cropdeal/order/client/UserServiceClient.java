package com.cropdeal.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "http://localhost:8081",
        configuration = FeignClientErrorDecoder.class
)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    Object getUserById(
            @PathVariable("id") Long id);
}