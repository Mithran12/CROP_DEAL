package com.cropdeal.review.controller;

import com.cropdeal.review.dto.*;
import com.cropdeal.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
	private final ReviewService service;

	public ReviewController(ReviewService s) {
		service = s;
	}

	@PostMapping
	public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest r) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
	}

	@GetMapping("/admin")
	public List<ReviewResponse> all() {
		return service.all();
	}

	@GetMapping("/user/{userId}")
	public List<ReviewResponse> forUser(@PathVariable Long userId) {
		return service.forUser(userId);
	}
}
