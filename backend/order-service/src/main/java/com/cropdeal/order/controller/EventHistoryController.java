package com.cropdeal.order.controller;

import com.cropdeal.order.entity.OrderEventRecord;
import com.cropdeal.order.service.EventSourcingService;
import com.cropdeal.order.security.SecurityUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/events")
public class EventHistoryController {
	private final EventSourcingService service;

	public EventHistoryController(EventSourcingService s) {
		service = s;
	}

	@GetMapping("/order/{orderId}")
	public ResponseEntity<List<OrderEventRecord>> orderHistory(@PathVariable Long orderId) {
		if (!SecurityUtils.hasRole("ADMIN"))
			throw new org.springframework.security.access.AccessDeniedException("Admin access required");
		return ResponseEntity.ok(service.history("ORDER", String.valueOf(orderId)));
	}

	@GetMapping("/negotiation/{id}")
	public ResponseEntity<List<OrderEventRecord>> negotiationHistory(@PathVariable Long id) {
		if (!SecurityUtils.hasRole("ADMIN"))
			throw new org.springframework.security.access.AccessDeniedException("Admin access required");
		return ResponseEntity.ok(service.history("NEGOTIATION", String.valueOf(id)));
	}
}
