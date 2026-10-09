package com.cropdeal.order.service;

import com.cropdeal.order.client.UserServiceClient;
import com.cropdeal.order.dto.*;
import com.cropdeal.order.entity.*;
import com.cropdeal.order.repository.OrderRepository;
import com.cropdeal.order.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class OrderQueryService {
	private final OrderRepository repo;
	private final UserServiceClient users;

	public OrderQueryService(OrderRepository r, UserServiceClient u) {
		repo = r;
		users = u;
	}

	public OrderResponse get(Long id) {
		Order o = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
		ensureParty(o);
		return map(o);
	}

	public List<OrderResponse> all() {
		if (!SecurityUtils.hasRole("ADMIN"))
			throw new AccessDeniedException("Admin access required");
		return repo.findAll().stream().map(this::map).toList();
	}

	public List<OrderResponse> dealer(Long id) {
		ensureOwner(id, "DEALER");
		return repo.findByDealerId(id).stream().map(this::map).toList();
	}

	public List<OrderResponse> farmer(Long id) {
		ensureOwner(id, "FARMER");
		return repo.findByFarmerId(id).stream().map(this::map).toList();
	}

	public List<OrderResponse> status(OrderStatus s) {
		if (!SecurityUtils.hasRole("ADMIN"))
			throw new AccessDeniedException("Admin access required");
		return repo.findByStatus(s).stream().map(this::map).toList();
	}

	private void ensureOwner(Long profileId, String role) {
		if (SecurityUtils.hasRole("ADMIN"))
			return;
		UserResponse u = users.getUser(profileId);
		if (!role.equalsIgnoreCase(u.getRole()) || !SecurityUtils.getCurrentAuthUserId().equals(u.getAuthUserId()))
			throw new AccessDeniedException("You can view only your own orders");
	}

	private void ensureParty(Order o) {
		if (SecurityUtils.hasRole("ADMIN"))
			return;
		Long auth = SecurityUtils.getCurrentAuthUserId();
		UserResponse f = users.getUser(o.getFarmerId()), d = users.getUser(o.getDealerId());
		if (!auth.equals(f.getAuthUserId()) && !auth.equals(d.getAuthUserId()))
			throw new AccessDeniedException("You can access only your own orders");
	}

	private OrderResponse map(Order o) {
		OrderResponse r = new OrderResponse();
		r.setId(o.getId());
		r.setOrderNumber(o.getOrderNumber());
		r.setFarmerId(o.getFarmerId());
		r.setDealerId(o.getDealerId());
		r.setCropId(o.getCropId());
		r.setQuantity(o.getQuantity());
		r.setAgreedPrice(o.getAgreedPrice());
		r.setTotalAmount(o.getTotalAmount());
		r.setStatus(o.getStatus());
		r.setPaymentStatus(o.getPaymentStatus());
		r.setPickupStatus(o.getPickupStatus());
		r.setCreatedAt(o.getCreatedAt());
		r.setUpdatedAt(o.getUpdatedAt());
		r.setCompletedAt(o.getCompletedAt());
		r.setDeliveryConfirmed(o.getDeliveryConfirmed());
		r.setDeliveryQrExpiresAt(o.getDeliveryQrExpiresAt());
		r.setDeliveryQrVerifiedAt(o.getDeliveryQrVerifiedAt());
		return r;
	}
}
