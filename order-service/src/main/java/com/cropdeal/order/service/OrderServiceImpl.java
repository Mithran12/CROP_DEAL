package com.cropdeal.order.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;

import com.cropdeal.order.client.CropServiceClient;
import com.cropdeal.order.client.UserServiceClient;
import com.cropdeal.order.client.WalletClient;
import com.cropdeal.order.dto.CropResponse;
import com.cropdeal.order.dto.DeliveryConfirmationRequest;
import com.cropdeal.order.dto.EscrowReleaseRequest;
import com.cropdeal.order.dto.OrderRequest;
import com.cropdeal.order.dto.OrderResponse;
import com.cropdeal.order.dto.UserResponse;
import com.cropdeal.order.entity.Order;
import com.cropdeal.order.entity.OrderStatus;
import com.cropdeal.order.entity.PaymentStatus;
import com.cropdeal.order.entity.PickupStatus;
import com.cropdeal.order.exception.OrderNotFoundException;
import com.cropdeal.order.messaging.OrderEvent;
import com.cropdeal.order.messaging.OrderEventProducer;
import com.cropdeal.order.repository.OrderRepository;
import com.cropdeal.order.repository.NegotiationRepository;
import com.cropdeal.order.entity.Negotiation;
import com.cropdeal.order.entity.NegotiationStatus;
import com.cropdeal.order.security.SecurityUtils;

@Service
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final UserServiceClient userServiceClient;
	private final CropServiceClient cropServiceClient;
	private final OrderEventProducer orderEventProducer;
	private final WalletClient walletClient;
	private final QrCodeService qrCodeService;
	private final NegotiationRepository negotiationRepository;
	private final EventSourcingService eventSourcingService;
	private final com.cropdeal.order.client.AuctionClient auctionClient;
	private final com.cropdeal.order.client.PaymentClient paymentClient;

	@Value("${internal.api.key}")
	private String internalApiKey;

	public OrderServiceImpl(OrderRepository orderRepository, UserServiceClient userServiceClient,
			CropServiceClient cropServiceClient, OrderEventProducer orderEventProducer, WalletClient walletClient,
			QrCodeService qrCodeService, NegotiationRepository negotiationRepository,
			EventSourcingService eventSourcingService, com.cropdeal.order.client.AuctionClient auctionClient,
			com.cropdeal.order.client.PaymentClient paymentClient) {

		this.orderRepository = orderRepository;
		this.userServiceClient = userServiceClient;
		this.cropServiceClient = cropServiceClient;
		this.orderEventProducer = orderEventProducer;
		this.walletClient = walletClient;
		this.qrCodeService = qrCodeService;
		this.negotiationRepository = negotiationRepository;
		this.eventSourcingService = eventSourcingService;
		this.auctionClient = auctionClient;
		this.paymentClient = paymentClient;
	}

	@Override
	public OrderResponse createOrder(OrderRequest request) {

		UserResponse dealer = userServiceClient.getUser(request.getDealerId());

		if (!SecurityUtils.getCurrentAuthUserId().equals(dealer.getAuthUserId())) {
			throw new AccessDeniedException("You can place orders only for your own dealer profile");
		}

		if (!"DEALER".equalsIgnoreCase(dealer.getRole())) {
			throw new IllegalArgumentException("Only dealers can place orders");

		}
		if (!dealer.isActive()) {
			throw new IllegalArgumentException("Dealer account is not active");

		}
		CropResponse crop = cropServiceClient.getCrop(request.getCropId());

		if (!"AVAILABLE".equalsIgnoreCase(crop.getStatus())) {
			throw new IllegalArgumentException("Crop is not active");
		}
		if (crop.getQuantity() < request.getQuantity()) {
			throw new IllegalArgumentException("Asked quantity is higher than available quantrity");
		}
		UserResponse farmer = userServiceClient.getUser(crop.getFarmerId());

		if (!"FARMER".equalsIgnoreCase(farmer.getRole())) {
			throw new IllegalArgumentException("Crop does not belong to a valid farmer");
		}

		if (!farmer.isActive()) {
			throw new IllegalArgumentException("Farmer account is not active");
		}
		Double totalAmount = request.getAgreedPrice() * request.getQuantity();

		Order order = new Order();

		order.setDealerId(request.getDealerId());

		order.setFarmerId(crop.getFarmerId());

		order.setCropId(request.getCropId());
		order.setQuantity(request.getQuantity());
		order.setAgreedPrice(request.getAgreedPrice());
		order.setTotalAmount(totalAmount);

		order.setStatus(OrderStatus.CREATED);
		order.setPaymentStatus(PaymentStatus.PENDING);
		order.setPickupStatus(PickupStatus.PENDING);
		order.setAuctionId(request.getAuctionId());

		order.setCreatedAt(LocalDateTime.now());
		order.setUpdatedAt(LocalDateTime.now());

		String orderNumber = "ORD-" + Year.now().getValue() + "-"
				+ UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		order.setOrderNumber(orderNumber);

		Order savedOrder = orderRepository.save(order);

		OrderEvent event = new OrderEvent();
		event.setOrderId(savedOrder.getId());
		event.setOrderNumber(savedOrder.getOrderNumber());
		event.setFarmerId(savedOrder.getFarmerId());
		event.setDealerId(savedOrder.getDealerId());
		event.setCropId(savedOrder.getCropId());
		event.setStatus(savedOrder.getStatus().name());
		event.setMessage("New order created: " + savedOrder.getOrderNumber());

		orderEventProducer.publishOrderCreated(event);

		return mapToResponse(savedOrder);
	}

	@Override
	public OrderResponse createOrderFromNegotiation(Long negotiationId) {
		Negotiation n = negotiationRepository.findById(negotiationId)
				.orElseThrow(() -> new IllegalArgumentException("Negotiation not found: " + negotiationId));
		if (n.getStatus() != NegotiationStatus.ACCEPTED) {
			throw new IllegalArgumentException("Order can be created only from an accepted negotiation");
		}
		UserResponse dealer = userServiceClient.getByAuthUserId(SecurityUtils.getCurrentAuthUserId());
		if (dealer == null || !dealer.getId().equals(n.getDealerId())) {
			throw new AccessDeniedException("Only the negotiating dealer can create the order");
		}

		// Prevent duplicate order creation
		if (n.getOrderId() != null) {
			Order existingOrder = orderRepository.findById(n.getOrderId()).orElse(null);
			if (existingOrder != null) {
				return mapToResponse(existingOrder);
			}
		}
		java.util.Optional<Order> existingOrderByNeg = orderRepository.findByNegotiationId(negotiationId);
		if (existingOrderByNeg.isPresent()) {
			Order existingOrder = existingOrderByNeg.get();
			n.setOrderId(existingOrder.getId());
			n.setOrderNumber(existingOrder.getOrderNumber());
			negotiationRepository.save(n);
			return mapToResponse(existingOrder);
		}

		CropResponse crop = cropServiceClient.getCrop(n.getCropId());
		if (!"AVAILABLE".equalsIgnoreCase(crop.getStatus()) || crop.getQuantity() <= 0) {
			throw new IllegalArgumentException("Crop is no longer available");
		}
		OrderRequest request = new OrderRequest();
		request.setDealerId(n.getDealerId());
		request.setCropId(n.getCropId());
		request.setQuantity(n.getQuantity());
		request.setAgreedPrice(n.getCurrentOffer());
		OrderResponse response = createOrder(request);

		// Negotiation was agreed; transition order to ACCEPTED so dealer can proceed to
		// payment
		Order savedOrder = orderRepository.findById(response.getId()).orElse(null);
		if (savedOrder != null) {
			savedOrder.setStatus(OrderStatus.ACCEPTED);
			savedOrder.setNegotiationId(negotiationId);
			savedOrder = orderRepository.save(savedOrder);
			response = mapToResponse(savedOrder);
		}

		// Save linked order on negotiation
		n.setOrderId(savedOrder != null ? savedOrder.getId() : response.getId());
		n.setOrderNumber(savedOrder != null ? savedOrder.getOrderNumber() : response.getOrderNumber());
		n.setUpdatedAt(LocalDateTime.now());
		negotiationRepository.save(n);

		eventSourcingService.append("ORDER", String.valueOf(response.getId()), "ORDER_CREATED_FROM_ACCEPTED_OFFER",
				java.util.Map.of("negotiationId", negotiationId, "finalAgreedPrice", n.getCurrentOffer()));
		return response;
	}

	@Override
	public OrderResponse createOrderFromAuction(Long auctionId) {
		com.cropdeal.order.dto.AuctionResponse auction = null;
		try {
			auction = auctionClient.getAuction(auctionId);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Auction not found: " + auctionId);
		}
		if (auction == null) {
			throw new IllegalArgumentException("Auction not found: " + auctionId);
		}

		String status = auction.getStatus();
		if (!"ENDED".equalsIgnoreCase(status) && !"COMPLETED".equalsIgnoreCase(status)) {
			throw new IllegalArgumentException("Order can only be created for an ended auction");
		}

		UserResponse dealer = userServiceClient.getByAuthUserId(SecurityUtils.getCurrentAuthUserId());
		if (dealer == null) {
			throw new AccessDeniedException("User not authenticated");
		}
		if (auction.getHighestBidderId() == null || !auction.getHighestBidderId().equals(dealer.getId())) {
			throw new AccessDeniedException("Only the winning dealer can create an order for this auction");
		}

		// Prevent duplicate order creation
		if (auction.getOrderId() != null) {
			Order existing = orderRepository.findById(auction.getOrderId()).orElse(null);
			if (existing != null) {
				return mapToResponse(existing);
			}
		}
		java.util.Optional<Order> existingOrderByAuction = orderRepository.findByAuctionId(auctionId);
		if (existingOrderByAuction.isPresent()) {
			Order existing = existingOrderByAuction.get();
			try {
				auctionClient.linkOrder(auctionId, existing.getId(), existing.getOrderNumber());
			} catch (Exception ignored) {
			}
			return mapToResponse(existing);
		}

		CropResponse crop = cropServiceClient.getCrop(auction.getCropId());
		if (crop == null) {
			throw new IllegalArgumentException("Crop not found for auction: " + auction.getCropId());
		}
		double quantity = (auction.getQuantity() != null && auction.getQuantity() > 0) ? auction.getQuantity()
				: ((crop.getQuantity() != null && crop.getQuantity() > 0) ? crop.getQuantity() : 1.0);

		try {
			cropServiceClient.reduceQuantity(crop.getId(), quantity);
		} catch (Exception ignored) {
		}

		Order order = new Order();
		order.setOrderNumber(
				"ORD-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		order.setFarmerId(crop.getFarmerId());
		order.setDealerId(dealer.getId());
		order.setCropId(crop.getId());
		order.setQuantity(quantity);
		order.setAgreedPrice(auction.getHighestBid());
		order.setTotalAmount(quantity * auction.getHighestBid());
		order.setStatus(OrderStatus.ACCEPTED);
		order.setPaymentStatus(PaymentStatus.PENDING);
		order.setPickupStatus(PickupStatus.PENDING);
		order.setAuctionId(auctionId);
		order.setCropReserved(true);
		order.setDeliveryQrCode(null);
		order.setDeliveryQrExpiresAt(null);
		order.setDeliveryConfirmed(false);
		order.setCreatedAt(LocalDateTime.now());
		order.setUpdatedAt(LocalDateTime.now());

		Order savedOrder = orderRepository.save(order);

		try {
			auctionClient.linkOrder(auctionId, savedOrder.getId(), savedOrder.getOrderNumber());
		} catch (Exception ex) {
			System.err.println("Could not link order to auction " + auctionId + ": " + ex.getMessage());
		}

		OrderEvent event = new OrderEvent();
		event.setOrderId(savedOrder.getId());
		event.setOrderNumber(savedOrder.getOrderNumber());
		event.setFarmerId(savedOrder.getFarmerId());
		event.setDealerId(savedOrder.getDealerId());
		event.setCropId(savedOrder.getCropId());
		event.setStatus(savedOrder.getStatus().name());
		event.setMessage("Auction winning order created: " + savedOrder.getOrderNumber() + ". Proceed to payment.");
		orderEventProducer.publishOrderCreated(event);

		eventSourcingService.append("ORDER", String.valueOf(savedOrder.getId()), "ORDER_CREATED_FROM_AUCTION",
				java.util.Map.of("auctionId", auctionId, "winningBid", auction.getHighestBid(), "amount",
						savedOrder.getTotalAmount()));

		return mapToResponse(savedOrder);
	}

	@Override
	public OrderResponse createAuctionOrder(OrderRequest request) {

		CropResponse crop = cropServiceClient.getCrop(request.getCropId());
		if (crop == null) {
			throw new IllegalArgumentException("Crop not found");
		}
		if (request.getQuantity() == null || request.getQuantity() <= 0) {
			throw new IllegalArgumentException("Invalid crop quantity");
		}
		if (request.getAgreedPrice() == null || request.getAgreedPrice() <= 0) {
			throw new IllegalArgumentException("Invalid agreed price");
		}

		try {
			cropServiceClient.reduceQuantity(request.getCropId(), request.getQuantity());
		} catch (Exception ignored) {
		}

		Order order = new Order();
		order.setOrderNumber(
				"ORD-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		order.setFarmerId(crop.getFarmerId());
		order.setDealerId(request.getDealerId());
		order.setCropId(request.getCropId());
		order.setQuantity(request.getQuantity());
		order.setAgreedPrice(request.getAgreedPrice());
		order.setTotalAmount(request.getQuantity() * request.getAgreedPrice());
		order.setStatus(OrderStatus.ACCEPTED);
		order.setPaymentStatus(PaymentStatus.PENDING);
		order.setPickupStatus(PickupStatus.PENDING);
		order.setAuctionId(request.getAuctionId());
		order.setCropReserved(true);
		order.setDeliveryQrCode(null);
		order.setDeliveryQrExpiresAt(null);
		order.setDeliveryConfirmed(false);
		order.setCreatedAt(LocalDateTime.now());
		order.setUpdatedAt(LocalDateTime.now());

		Order savedOrder = orderRepository.save(order);

		OrderEvent event = new OrderEvent();
		event.setOrderId(savedOrder.getId());
		event.setOrderNumber(savedOrder.getOrderNumber());
		event.setFarmerId(savedOrder.getFarmerId());
		event.setDealerId(savedOrder.getDealerId());
		event.setCropId(savedOrder.getCropId());
		event.setStatus(savedOrder.getStatus().name());
		event.setMessage("Auction order created: " + savedOrder.getOrderNumber());
		orderEventProducer.publishOrderCreated(event);

		return mapToResponse(savedOrder);
	}

	@Override
	public OrderResponse completeOrder(Long id) {

		Order order = findOrder(id);
		ensureOrderPartyOrAdmin(order);

		if (order.getStatus() != OrderStatus.PAID || order.getPaymentStatus() != PaymentStatus.SUCCESS) {
			throw new IllegalArgumentException("Only paid orders can be completed");
		}

		order.setStatus(OrderStatus.COMPLETED);
		order.setPickupStatus(PickupStatus.COMPLETED);
		order.setCompletedAt(LocalDateTime.now());
		order.setUpdatedAt(LocalDateTime.now());

		Order updatedOrder = orderRepository.save(order);

		if (updatedOrder.getAuctionId() != null) {
			try {
				auctionClient.completeAuction(updatedOrder.getAuctionId());
			} catch (Exception ex) {
				System.err
						.println("Could not complete auction " + updatedOrder.getAuctionId() + ": " + ex.getMessage());
			}
		}
		if (updatedOrder.getNegotiationId() != null) {
			try {
				negotiationRepository.findById(updatedOrder.getNegotiationId()).ifPresent(neg -> {
					neg.setStatus(NegotiationStatus.COMPLETED);
					neg.setUpdatedAt(LocalDateTime.now());
					negotiationRepository.save(neg);
				});
			} catch (Exception ex) {
				System.err.println(
						"Could not complete negotiation " + updatedOrder.getNegotiationId() + ": " + ex.getMessage());
			}
		} else {
			try {
				negotiationRepository.findByOrderId(updatedOrder.getId()).ifPresent(neg -> {
					neg.setStatus(NegotiationStatus.COMPLETED);
					neg.setUpdatedAt(LocalDateTime.now());
					negotiationRepository.save(neg);
				});
			} catch (Exception ignored) {
			}
		}

		OrderEvent event = new OrderEvent();

		event.setOrderId(updatedOrder.getId());
		event.setOrderNumber(updatedOrder.getOrderNumber());
		event.setFarmerId(updatedOrder.getFarmerId());
		event.setDealerId(updatedOrder.getDealerId());
		event.setCropId(updatedOrder.getCropId());
		event.setStatus(updatedOrder.getStatus().name());
		event.setMessage("Order completed: " + updatedOrder.getOrderNumber());

		orderEventProducer.publishOrderCompleted(event);

		return mapToResponse(updatedOrder);
	}

	@Override
	public OrderResponse getOrderById(Long id) {

		Order order = findOrder(id);
		ensureOrderPartyOrAdmin(order);

		return mapToResponse(order);
	}

	@Override
	public java.util.Optional<OrderResponse> getOrderByAuctionId(Long auctionId) {
		return orderRepository.findByAuctionId(auctionId).map(this::mapToResponse);
	}

	@Override
	public List<OrderResponse> getAllOrders() {

		requireAdmin();
		return orderRepository.findAll().stream().map(this::mapToResponse).toList();
	}

	@Override
	public List<OrderResponse> getOrdersByDealer(Long dealerId) {

		UserResponse dealer = userServiceClient.getUser(dealerId);
		if (!SecurityUtils.hasRole("ADMIN") && !SecurityUtils.getCurrentAuthUserId().equals(dealer.getAuthUserId())) {
			throw new AccessDeniedException("You can view only your own dealer orders");
		}

		return orderRepository.findByDealerId(dealerId).stream().map(this::mapToResponse).toList();
	}

	@Override
	public List<OrderResponse> getOrdersByFarmer(Long farmerId) {

		UserResponse farmer = userServiceClient.getUser(farmerId);
		if (!SecurityUtils.hasRole("ADMIN") && !SecurityUtils.getCurrentAuthUserId().equals(farmer.getAuthUserId())) {
			throw new AccessDeniedException("You can view only your own farmer orders");
		}

		return orderRepository.findByFarmerId(farmerId).stream().map(this::mapToResponse).toList();
	}

	@Override
	public List<OrderResponse> getOrdersByStatus(OrderStatus status) {

		requireAdmin();
		return orderRepository.findByStatus(status).stream().map(this::mapToResponse).toList();
	}

	@Override
	public OrderResponse acceptOrder(Long id) {

		Order order = findOrder(id);
		ensureFarmerOwner(order);

		if (order.getStatus() != OrderStatus.CREATED) {
			throw new IllegalArgumentException("Only newly created orders can be accepted");
		}

		order.setStatus(OrderStatus.ACCEPTED);
		order.setUpdatedAt(LocalDateTime.now());

		Order updatedOrder = orderRepository.save(order);

		OrderEvent event = new OrderEvent();
		event.setOrderId(updatedOrder.getId());
		event.setOrderNumber(updatedOrder.getOrderNumber());
		event.setFarmerId(updatedOrder.getFarmerId());
		event.setDealerId(updatedOrder.getDealerId());
		event.setCropId(updatedOrder.getCropId());
		event.setStatus(updatedOrder.getStatus().name());
		event.setMessage("Order accepted: " + updatedOrder.getOrderNumber());

		orderEventProducer.publishOrderAccepted(event);

		return mapToResponse(updatedOrder);
	}

	@Override
	public OrderResponse rejectOrder(Long id) {

		Order order = findOrder(id);
		ensureFarmerOwner(order);

		if (order.getStatus() != OrderStatus.CREATED) {
			throw new IllegalArgumentException("Only newly created orders can be rejected");
		}

		order.setStatus(OrderStatus.REJECTED);
		order.setUpdatedAt(LocalDateTime.now());

		Order updatedOrder = orderRepository.save(order);

		OrderEvent event = new OrderEvent();
		event.setOrderId(updatedOrder.getId());
		event.setOrderNumber(updatedOrder.getOrderNumber());
		event.setFarmerId(updatedOrder.getFarmerId());
		event.setDealerId(updatedOrder.getDealerId());
		event.setCropId(updatedOrder.getCropId());
		event.setStatus(updatedOrder.getStatus().name());
		event.setMessage("Order rejected: " + updatedOrder.getOrderNumber());

		orderEventProducer.publishOrderRejected(event);

		return mapToResponse(updatedOrder);
	}

	@Override
	public OrderResponse cancelOrder(Long id) {

		Order order = findOrder(id);
		ensureDealerOwner(order);

		if (order.getAuctionId() != null) {
			throw new IllegalArgumentException("Auction-generated orders cannot be cancelled");
		}

		if (order.getStatus() == OrderStatus.CANCELLED) {
			throw new IllegalArgumentException("Order is already cancelled");
		}
		if (order.getStatus() == OrderStatus.COMPLETED) {
			throw new IllegalArgumentException("Completed orders cannot be cancelled");
		}
		if (Boolean.TRUE.equals(order.getDeliveryConfirmed())) {
			throw new IllegalArgumentException("Order delivery has already been confirmed and cannot be cancelled");
		}
		if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.ACCEPTED
				&& order.getStatus() != OrderStatus.PAID) {
			throw new IllegalArgumentException("Only created, accepted, or unfulfilled paid orders can be cancelled");
		}

		boolean wasPaid = (order.getStatus() == OrderStatus.PAID || order.getPaymentStatus() == PaymentStatus.SUCCESS);

		// 1. If paid, cancel payment and refund escrow to dealer's wallet
		if (wasPaid) {
			try {
				paymentClient.cancelPayment(order.getId(), internalApiKey);
			} catch (Exception ex) {
				// Log and continue to ensure order is marked cancelled
			}
			order.setPaymentStatus(PaymentStatus.REFUNDED);
		}

		// 2. Invalidate delivery QR code
		order.setDeliveryQrCode(null);
		order.setDeliveryQrExpiresAt(null);

		// 3. Restore crop quantity if reserved
		// 3. Restore crop quantity when the order is cancelled
		if (order.getCropId() != null && order.getQuantity() != null && order.getQuantity() > 0) {
			try {
				cropServiceClient.restoreQuantity(order.getCropId(), order.getQuantity());

				order.setCropReserved(false);

			} catch (Exception ex) {
				throw new IllegalStateException(
						"Order was not cancelled because crop quantity " + "could not be restored: " + ex.getMessage(),
						ex);
			}
		}

		order.setStatus(OrderStatus.CANCELLED);
		order.setUpdatedAt(LocalDateTime.now());

		Order updatedOrder = orderRepository.save(order);

		// 4. Publish order cancelled event
		OrderEvent event = new OrderEvent();
		event.setOrderId(updatedOrder.getId());
		event.setOrderNumber(updatedOrder.getOrderNumber());
		event.setFarmerId(updatedOrder.getFarmerId());
		event.setDealerId(updatedOrder.getDealerId());
		event.setCropId(updatedOrder.getCropId());
		event.setStatus(updatedOrder.getStatus().name());
		event.setMessage(
				"Order cancelled: " + updatedOrder.getOrderNumber() + (wasPaid ? " (Refunded to wallet)" : ""));
		orderEventProducer.publishOrderCancelled(event);

		return mapToResponse(updatedOrder);
	}

	@Override
	public void deleteOrder(Long id) {

		requireAdmin();
		Order order = findOrder(id);

		orderRepository.delete(order);
	}

	@Override
	public OrderResponse markPaymentSuccess(Long id) {

		Order order = findOrder(id);

		if (order.getStatus() == OrderStatus.PAID && order.getPaymentStatus() == PaymentStatus.SUCCESS) {
			return mapToResponse(order);
		}

		if (order.getStatus() != OrderStatus.ACCEPTED && order.getStatus() != OrderStatus.CREATED
				&& order.getStatus() != OrderStatus.PAYMENT_FAILED) {
			throw new IllegalArgumentException("Payment can be completed only for an accepted order");
		}

		if (!Boolean.TRUE.equals(order.getCropReserved())) {
			cropServiceClient.reduceQuantity(order.getCropId(), order.getQuantity());
		}

		order.setPaymentStatus(PaymentStatus.SUCCESS);
		order.setStatus(OrderStatus.PAID);
		if (order.getDeliveryQrCode() == null) {
			order.setDeliveryQrCode(generateSecureToken());
			order.setDeliveryQrExpiresAt(LocalDateTime.now().plusHours(24));
			order.setDeliveryConfirmed(false);
		}
		order.setUpdatedAt(LocalDateTime.now());

		Order updatedOrder = orderRepository.save(order);
		OrderEvent qrEvent = new OrderEvent();
		qrEvent.setOrderId(updatedOrder.getId());
		qrEvent.setOrderNumber(updatedOrder.getOrderNumber());
		qrEvent.setFarmerId(updatedOrder.getFarmerId());
		qrEvent.setDealerId(updatedOrder.getDealerId());
		qrEvent.setCropId(updatedOrder.getCropId());
		qrEvent.setStatus("DELIVERY_QR_READY");
		qrEvent.setMessage("Delivery QR/token is ready for " + updatedOrder.getOrderNumber());
		orderEventProducer.publishDeliveryQrReady(qrEvent);
		eventSourcingService.append("ORDER", String.valueOf(updatedOrder.getId()), "PAYMENT_SUCCESS",
				java.util.Map.of("amount", updatedOrder.getTotalAmount(), "qrGenerated", true));
		eventSourcingService.append("ORDER", String.valueOf(updatedOrder.getId()), "ORDER_CONFIRMED",
				java.util.Map.of("status", updatedOrder.getStatus().name()));
		eventSourcingService.append("ORDER", String.valueOf(updatedOrder.getId()), "DELIVERY_QR_GENERATED",
				java.util.Map.of("expiresAt", updatedOrder.getDeliveryQrExpiresAt().toString()));

		return mapToResponse(updatedOrder);
	}

	@Override
	public OrderResponse markPaymentFailed(Long id) {

		Order order = findOrder(id);

		if (order.getStatus() != OrderStatus.ACCEPTED && order.getStatus() != OrderStatus.PAYMENT_FAILED) {
			throw new IllegalArgumentException("Payment can fail only for an accepted order");
		}

		order.setPaymentStatus(PaymentStatus.FAILED);
		order.setStatus(OrderStatus.PAYMENT_FAILED);
		order.setUpdatedAt(LocalDateTime.now());

		Order updatedOrder = orderRepository.save(order);
		OrderEvent failedEvent = new OrderEvent();
		failedEvent.setOrderId(updatedOrder.getId());
		failedEvent.setOrderNumber(updatedOrder.getOrderNumber());
		failedEvent.setFarmerId(updatedOrder.getFarmerId());
		failedEvent.setDealerId(updatedOrder.getDealerId());
		failedEvent.setCropId(updatedOrder.getCropId());
		failedEvent.setStatus("PAYMENT_FAILED");
		failedEvent.setMessage("Payment failed for " + updatedOrder.getOrderNumber());
		orderEventProducer.publishOrderRejected(failedEvent);
		eventSourcingService.append("ORDER", String.valueOf(updatedOrder.getId()), "PAYMENT_FAILED",
				java.util.Map.of("orderId", updatedOrder.getId()));

		return mapToResponse(updatedOrder);
	}

	@Override
	public OrderResponse confirmDelivery(Long orderId, DeliveryConfirmationRequest request) {

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found"));

		if (!SecurityUtils.hasRole("ADMIN")) {
			ensureDealerOwner(order);
		}

		if (Boolean.TRUE.equals(order.getDeliveryConfirmed())) {
			throw new IllegalArgumentException("Delivery already confirmed");
		}

		if (order.getDeliveryQrCode() == null || !order.getDeliveryQrCode().equals(request.getQrCode())) {
			throw new IllegalArgumentException("Invalid delivery QR code");
		}
		if (order.getDeliveryQrExpiresAt() != null && order.getDeliveryQrExpiresAt().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Delivery QR code has expired");
		}
		if (order.getDeliveryQrVerifiedAt() != null) {
			throw new IllegalArgumentException("Delivery QR code has already been used");
		}
		if (order.getStatus() != OrderStatus.PAID || order.getPaymentStatus() != PaymentStatus.SUCCESS) {
			throw new IllegalArgumentException("Only paid orders awaiting delivery can be verified");
		}

		double totalAmount = order.getQuantity() * order.getAgreedPrice();

		EscrowReleaseRequest escrowRequest = new EscrowReleaseRequest();
		escrowRequest.setFarmerId(order.getFarmerId());
		escrowRequest.setAmount(totalAmount);
		escrowRequest.setReference("ORDER-" + order.getId());

		walletClient.releaseEscrow(order.getDealerId(), internalApiKey, escrowRequest);

		order.setDeliveryConfirmed(true);
		order.setDeliveryQrVerifiedAt(LocalDateTime.now());
		order.setPickupStatus(PickupStatus.COMPLETED);
		order.setStatus(OrderStatus.COMPLETED);
		order.setCompletedAt(LocalDateTime.now());
		order.setUpdatedAt(LocalDateTime.now());

		Order savedOrder = orderRepository.save(order);

		if (savedOrder.getAuctionId() != null) {
			try {
				auctionClient.completeAuction(savedOrder.getAuctionId());
			} catch (Exception ex) {
				System.err.println("Could not complete auction " + savedOrder.getAuctionId() + ": " + ex.getMessage());
			}
		}
		if (savedOrder.getNegotiationId() != null) {
			try {
				negotiationRepository.findById(savedOrder.getNegotiationId()).ifPresent(neg -> {
					neg.setStatus(NegotiationStatus.COMPLETED);
					neg.setUpdatedAt(LocalDateTime.now());
					negotiationRepository.save(neg);
				});
			} catch (Exception ex) {
				System.err.println(
						"Could not complete negotiation " + savedOrder.getNegotiationId() + ": " + ex.getMessage());
			}
		} else {
			try {
				negotiationRepository.findByOrderId(savedOrder.getId()).ifPresent(neg -> {
					neg.setStatus(NegotiationStatus.COMPLETED);
					neg.setUpdatedAt(LocalDateTime.now());
					negotiationRepository.save(neg);
				});
			} catch (Exception ignored) {
			}
		}

		OrderEvent event = new OrderEvent();
		event.setOrderId(savedOrder.getId());
		event.setOrderNumber(savedOrder.getOrderNumber());
		event.setFarmerId(savedOrder.getFarmerId());
		event.setDealerId(savedOrder.getDealerId());
		event.setCropId(savedOrder.getCropId());
		event.setStatus(savedOrder.getStatus().name());
		event.setMessage("Order completed after QR delivery confirmation: " + savedOrder.getOrderNumber());
		orderEventProducer.publishOrderCompleted(event);
		eventSourcingService.append("ORDER", String.valueOf(savedOrder.getId()), "DELIVERY_VERIFIED",
				java.util.Map.of("dealerId", savedOrder.getDealerId()));
		eventSourcingService.append("ORDER", String.valueOf(savedOrder.getId()), "ORDER_COMPLETED",
				java.util.Map.of("completedAt", savedOrder.getCompletedAt().toString(), "paymentReleased", true));

		return mapToResponse(savedOrder);
	}

	public String getDeliveryQrToken(Long orderId) {
		Order order = findOrder(orderId);
		if (!SecurityUtils.hasRole("ADMIN"))
			ensureFarmerOwner(order);
		if (order.getStatus() != OrderStatus.PAID)
			throw new IllegalArgumentException("Delivery token is available only after payment succeeds");
		if (order.getDeliveryQrVerifiedAt() != null)
			throw new IllegalArgumentException("Delivery QR token has already been used");
		if (order.getDeliveryQrCode() == null) {
			order.setDeliveryQrCode(generateSecureToken());
			order.setDeliveryQrExpiresAt(LocalDateTime.now().plusHours(24));
			order.setDeliveryConfirmed(false);
			orderRepository.save(order);
		}
		if (order.getDeliveryQrExpiresAt() != null && order.getDeliveryQrExpiresAt().isBefore(LocalDateTime.now()))
			throw new IllegalArgumentException("Delivery QR token has expired");
		return order.getDeliveryQrCode();
	}

	@Override
	public byte[] getDeliveryQrCode(Long orderId) {
		Order order = findOrder(orderId);

		if (!SecurityUtils.hasRole("ADMIN")) {
			ensureFarmerOwner(order);
		}

		if (order.getDeliveryQrCode() == null || order.getDeliveryQrExpiresAt() == null) {
			throw new IllegalArgumentException("Delivery QR code is not available for this order");
		}
		if (order.getDeliveryQrVerifiedAt() != null) {
			throw new IllegalArgumentException("Delivery QR code has already been used");
		}
		if (order.getDeliveryQrExpiresAt().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Delivery QR code has expired");
		}
		if (order.getStatus() != OrderStatus.PAID) {
			throw new IllegalArgumentException("QR is available only after payment succeeds");
		}

		return qrCodeService.generateQrCode(order.getDeliveryQrCode());
	}

	private void ensureFarmerOwner(Order order) {
		UserResponse farmer = userServiceClient.getUser(order.getFarmerId());
		if (!SecurityUtils.getCurrentAuthUserId().equals(farmer.getAuthUserId())) {
			throw new AccessDeniedException("Only the order farmer can perform this action");
		}
	}

	private void ensureDealerOwner(Order order) {
		UserResponse dealer = userServiceClient.getUser(order.getDealerId());
		if (!SecurityUtils.getCurrentAuthUserId().equals(dealer.getAuthUserId())) {
			throw new AccessDeniedException("Only the order dealer can perform this action");
		}
	}

	private void ensureOrderPartyOrAdmin(Order order) {
		if (SecurityUtils.hasRole("ADMIN")) {
			return;
		}
		Long currentUserId = SecurityUtils.getCurrentAuthUserId();
		UserResponse farmer = userServiceClient.getUser(order.getFarmerId());
		UserResponse dealer = userServiceClient.getUser(order.getDealerId());
		if (!currentUserId.equals(farmer.getAuthUserId()) && !currentUserId.equals(dealer.getAuthUserId())) {
			throw new AccessDeniedException("You can access only your own orders");
		}
	}

	private void requireAdmin() {
		if (!SecurityUtils.hasRole("ADMIN")) {
			throw new AccessDeniedException("Admin access required");
		}
	}

	private Order findOrder(Long id) {

		return orderRepository.findById(id)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
	}

	private String generateSecureToken() {
		byte[] bytes = new byte[32];
		new SecureRandom().nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private OrderResponse mapToResponse(Order order) {

		OrderResponse response = new OrderResponse();

		response.setId(order.getId());
		response.setOrderNumber(order.getOrderNumber());
		response.setFarmerId(order.getFarmerId());
		response.setDealerId(order.getDealerId());
		response.setCropId(order.getCropId());
		response.setQuantity(order.getQuantity());
		response.setAgreedPrice(order.getAgreedPrice());
		response.setTotalAmount(order.getTotalAmount());
		response.setStatus(order.getStatus());
		response.setPaymentStatus(order.getPaymentStatus());
		response.setPickupStatus(order.getPickupStatus());
		response.setCreatedAt(order.getCreatedAt());
		response.setUpdatedAt(order.getUpdatedAt());
		response.setCompletedAt(order.getCompletedAt());
		response.setDeliveryConfirmed(order.getDeliveryConfirmed());
		response.setDeliveryQrToken(order.getDeliveryQrCode());
		response.setCropReserved(order.getCropReserved());
		response.setDeliveryQrExpiresAt(order.getDeliveryQrExpiresAt());
		response.setDeliveryQrVerifiedAt(order.getDeliveryQrVerifiedAt());
		response.setAuctionId(order.getAuctionId());
		response.setNegotiationId(order.getNegotiationId());

		return response;
	}

	@Override
	public OrderResponse getOrderByIdInternal(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
		return mapToResponse(order);
	}
}