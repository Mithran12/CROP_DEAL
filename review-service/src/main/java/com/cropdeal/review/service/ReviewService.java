package com.cropdeal.review.service;

import com.cropdeal.review.dto.*;
import com.cropdeal.review.entity.*;
import com.cropdeal.review.repository.ReviewRepository;
import com.cropdeal.review.client.*;
import com.cropdeal.review.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {
	private final ReviewRepository repo;
	private final OrderClient orders;
	private final UserClient users;
	@Value("${internal.api.key}")
	private String internalKey;

	@org.springframework.beans.factory.annotation.Autowired
	public ReviewService(ReviewRepository r, OrderClient o, UserClient u) {
		repo = r;
		orders = o;
		users = u;
	}

	public ReviewService(ReviewRepository r, OrderClient o, UserClient u, String key) {
		repo = r;
		orders = o;
		users = u;
		this.internalKey = key;
	}

	public ReviewResponse create(ReviewRequest req) {
		OrderResponse order = orders.getOrder(req.getOrderId(), internalKey);
		if (order == null)
			throw new IllegalArgumentException("Order not found with id: " + req.getOrderId());
		if (!"COMPLETED".equalsIgnoreCase(order.getStatus()))
			throw new IllegalArgumentException("Reviews are allowed only after order completion");
		UserResponse current = users.getByAuth(SecurityUtils.authId());
		ReviewType type;
		Long target;
		if (current.getId().equals(order.getFarmerId()) && "FARMER".equalsIgnoreCase(current.getRole())) {
			type = ReviewType.FARMER_TO_DEALER;
			target = order.getDealerId();
		} else if (current.getId().equals(order.getDealerId()) && "DEALER".equalsIgnoreCase(current.getRole())) {
			type = ReviewType.DEALER_TO_FARMER;
			target = order.getFarmerId();
		} else
			throw new AccessDeniedException("Only the farmer or dealer involved in the completed order can review it");
		if (repo.findByOrderIdAndReviewerIdAndReviewType(req.getOrderId(), current.getId(), type).isPresent())
			throw new IllegalArgumentException("You already reviewed this completed order");
		Review r = new Review();
		r.setOrderId(order.getId());
		r.setReviewerId(current.getId());
		r.setTargetUserId(target);
		r.setReviewType(type);
		r.setRating(req.getRating());
		r.setReview(req.getReview());
		r.setCreatedAt(LocalDateTime.now());
		return map(repo.save(r));
	}

	public List<ReviewResponse> all() {
		if (!SecurityUtils.admin())
			throw new AccessDeniedException("Admin access required");
		return repo.findAll().stream().map(this::map).toList();
	}

	public List<ReviewResponse> forUser(Long target) {
		if (!SecurityUtils.admin()) {
			UserResponse current = users.getByAuth(SecurityUtils.authId());
			if (!current.getId().equals(target))
				throw new AccessDeniedException("You can view only your own reviews");
		}
		return repo.findByTargetUserIdOrderByCreatedAtDesc(target).stream().map(this::map).toList();
	}

	private ReviewResponse map(Review r) {
		ReviewResponse x = new ReviewResponse();
		x.setId(r.getId());
		x.setOrderId(r.getOrderId());
		x.setReviewerId(r.getReviewerId());
		x.setTargetUserId(r.getTargetUserId());
		x.setReviewType(r.getReviewType());
		x.setRating(r.getRating());
		x.setReview(r.getReview());
		x.setCreatedAt(r.getCreatedAt());
		return x;
	}
}
