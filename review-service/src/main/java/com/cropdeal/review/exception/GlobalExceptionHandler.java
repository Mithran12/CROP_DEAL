package com.cropdeal.review.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<Map<String, Object>> bad(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now(), "error", e.getMessage()));
	}

	@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
	ResponseEntity<Map<String, Object>> denied(Exception e) {
		return ResponseEntity.status(403).body(Map.of("timestamp", Instant.now(), "error", e.getMessage()));
	}
}
