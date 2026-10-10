package com.cropdeal.review.client;

import com.cropdeal.review.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {
	@GetMapping("/users/{id}")
	UserResponse getUser(@PathVariable("id") Long id);

	@GetMapping("/users/auth/{authUserId}")
	UserResponse getByAuth(@PathVariable("authUserId") Long id);
}
