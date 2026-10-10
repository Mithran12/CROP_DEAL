package com.cropdeal.order.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.cropdeal.order.client.CropServiceClient;
import com.cropdeal.order.client.UserServiceClient;
import com.cropdeal.order.dto.CropResponse;
import com.cropdeal.order.dto.NegotiationRequest;
import com.cropdeal.order.dto.NegotiationResponse;
import com.cropdeal.order.dto.OfferRequest;
import com.cropdeal.order.dto.OfferResponse;
import com.cropdeal.order.dto.UserResponse;
import com.cropdeal.order.entity.Negotiation;
import com.cropdeal.order.entity.NegotiationStatus;
import com.cropdeal.order.entity.Offer;
import com.cropdeal.order.repository.NegotiationRepository;
import com.cropdeal.order.repository.OfferRepository;
import com.cropdeal.order.security.SecurityUtils;

import com.cropdeal.order.messaging.OrderEvent;
import com.cropdeal.order.messaging.OrderEventProducer;

@Service
public class NegotiationService {

	private final NegotiationRepository negotiations;
	private final OfferRepository offers;
	private final CropServiceClient crops;
	private final UserServiceClient users;
	private final EventSourcingService events;
	private final OrderEventProducer orderEventProducer;
	private final com.cropdeal.order.repository.OrderRepository orderRepository;

	@Autowired
	public NegotiationService(NegotiationRepository negotiations, OfferRepository offers, CropServiceClient crops,
			UserServiceClient users, EventSourcingService events, OrderEventProducer orderEventProducer,
			com.cropdeal.order.repository.OrderRepository orderRepository) {
		this.negotiations = negotiations;
		this.offers = offers;
		this.crops = crops;
		this.users = users;
		this.events = events;
		this.orderEventProducer = orderEventProducer;
		this.orderRepository = orderRepository;
	}

	public NegotiationService(NegotiationRepository negotiations, OfferRepository offers, CropServiceClient crops,
			UserServiceClient users, EventSourcingService events) {
		this(negotiations, offers, crops, users, events, null, null);
	}

	public NegotiationResponse start(NegotiationRequest req) {

		UserResponse dealer = users.getByAuthUserId(SecurityUtils.getCurrentAuthUserId());

		if (dealer == null) {
			throw new AccessDeniedException("Dealer not found");
		}

		if (!"DEALER".equalsIgnoreCase(dealer.getRole()) || !dealer.isActive()
				|| !dealer.getId().equals(req.getDealerId())) {
			throw new AccessDeniedException("Only the authenticated dealer can start a negotiation");
		}

		CropResponse crop = crops.getCrop(req.getCropId());

		if (crop == null) {
			throw new IllegalArgumentException("Crop not found");
		}

		if (!"AVAILABLE".equalsIgnoreCase(crop.getStatus()) || crop.getQuantity() <= 0 || req.getQuantity() <= 0
				|| req.getQuantity() > crop.getQuantity()) {
			throw new IllegalArgumentException("Crop is not available");
		}

		if (req.getAmount() <= 0) {
			throw new IllegalArgumentException("Offer amount must be positive");
		}

		if (req.getAmount() >= crop.getExpectedPrice()) {
			throw new IllegalArgumentException("Dealer offer must be below the farmer asking price");
		}

		Negotiation negotiation = new Negotiation();

		negotiation.setCropId(crop.getId());
		negotiation.setQuantity(req.getQuantity());
		negotiation.setFarmerId(crop.getFarmerId());
		negotiation.setDealerId(dealer.getId());
		negotiation.setAskingPrice(crop.getExpectedPrice());
		negotiation.setCurrentOffer(req.getAmount());
		negotiation.setStatus(NegotiationStatus.OPEN);
		negotiation.setCreatedAt(LocalDateTime.now());
		negotiation.setUpdatedAt(LocalDateTime.now());

		negotiation = negotiations.save(negotiation);

		addOffer(negotiation, "FARMER", crop.getFarmerId(), crop.getExpectedPrice(), "ASKING_PRICE");

		addOffer(negotiation, "DEALER", dealer.getId(), req.getAmount(), "PENDING");

		events.append("NEGOTIATION", String.valueOf(negotiation.getId()), "NEGOTIATION_CREATED",
				Map.of("cropId", crop.getId(), "farmerId", crop.getFarmerId(), "dealerId", dealer.getId(),
						"askingPrice", crop.getExpectedPrice(), "initialDealerOffer", req.getAmount()));

		OrderEvent startEvt = new OrderEvent();
		startEvt.setFarmerId(crop.getFarmerId());
		startEvt.setDealerId(dealer.getId());
		startEvt.setRecipientId(crop.getFarmerId());
		startEvt.setCropId(crop.getId());
		startEvt.setStatus("NEGOTIATION_STARTED");
		startEvt.setMessage("New negotiation started for " + (crop.getCropName() != null ? crop.getCropName() : "crop")
				+ ": dealer offered ₹" + req.getAmount() + "/qtl");
		sendNegotiationEvent(startEvt);

		return map(negotiation);
	}

	public NegotiationResponse dealerOffer(Long id, OfferRequest req) {

		Negotiation negotiation = findNegotiation(id);

		UserResponse user = users.getByAuthUserId(SecurityUtils.getCurrentAuthUserId());

		requireDealer(negotiation, user);
		ensureOpen(negotiation);

		if (req.getAmount() <= 0) {
			throw new IllegalArgumentException("Offer amount must be positive");
		}

		if (req.getAmount() >= negotiation.getAskingPrice()) {
			throw new IllegalArgumentException("Dealer offer must be below the farmer asking price");
		}

		negotiation.setCurrentOffer(req.getAmount());
		negotiation.setStatus(NegotiationStatus.OPEN);
		negotiation.setUpdatedAt(LocalDateTime.now());

		negotiations.save(negotiation);

		addOffer(negotiation, "DEALER", user.getId(), req.getAmount(), "PENDING");

		events.append("NEGOTIATION", String.valueOf(id), "OFFER_CREATED",
				Map.of("actorId", user.getId(), "amount", req.getAmount()));

		OrderEvent offerEvt = new OrderEvent();
		offerEvt.setFarmerId(negotiation.getFarmerId());
		offerEvt.setDealerId(user.getId());
		offerEvt.setRecipientId(negotiation.getFarmerId());
		offerEvt.setCropId(negotiation.getCropId());
		offerEvt.setStatus("NEGOTIATION_OFFER");
		offerEvt.setMessage("Dealer submitted revised offer of ₹" + req.getAmount() + "/qtl for negotiation #" + id);
		sendNegotiationEvent(offerEvt);

		return map(negotiation);
	}

	public NegotiationResponse counter(Long id, OfferRequest req) {

		Negotiation negotiation = findNegotiation(id);

		UserResponse user = users.getByAuthUserId(SecurityUtils.getCurrentAuthUserId());

		requireFarmer(negotiation, user);
		ensureOpen(negotiation);

		if (req.getAmount() <= 0) {
			throw new IllegalArgumentException("Counter amount must be positive");
		}

		negotiation.setCurrentOffer(req.getAmount());
		negotiation.setStatus(NegotiationStatus.COUNTERED);
		negotiation.setUpdatedAt(LocalDateTime.now());

		negotiations.save(negotiation);

		addOffer(negotiation, "FARMER", user.getId(), req.getAmount(), "COUNTERED");

		events.append("NEGOTIATION", String.valueOf(id), "OFFER_COUNTERED",
				Map.of("actorId", user.getId(), "amount", req.getAmount()));

		OrderEvent counterEvt = new OrderEvent();
		counterEvt.setFarmerId(user.getId());
		counterEvt.setDealerId(negotiation.getDealerId());
		counterEvt.setRecipientId(negotiation.getDealerId());
		counterEvt.setCropId(negotiation.getCropId());
		counterEvt.setStatus("NEGOTIATION_COUNTER");
		counterEvt.setMessage("Farmer proposed counter-offer of ₹" + req.getAmount() + "/qtl for negotiation #" + id);
		sendNegotiationEvent(counterEvt);

		return map(negotiation);
	}

	public NegotiationResponse accept(Long id) {

		Negotiation negotiation = findNegotiation(id);

		UserResponse user = users.getByAuthUserId(SecurityUtils.getCurrentAuthUserId());

		requireParticipant(negotiation, user);
		ensureOpen(negotiation);
		List<Offer> offerList = offers.findByNegotiationIdOrderByCreatedAtAsc(negotiation.getId());
		if (offerList.isEmpty())
			throw new IllegalArgumentException("No offer is available to accept");
		Offer lastOffer = offerList.get(offerList.size() - 1);
		if (lastOffer.getActorId().equals(user.getId())) {
			throw new AccessDeniedException("You cannot accept your own offer. Wait for the other party to respond.");
		}

		negotiation.setStatus(NegotiationStatus.ACCEPTED);
		negotiation.setAcceptedAt(LocalDateTime.now());
		negotiation.setUpdatedAt(LocalDateTime.now());

		negotiations.save(negotiation);

		markLastPendingAccepted(negotiation);

		events.append("NEGOTIATION", String.valueOf(id), "OFFER_ACCEPTED",
				Map.of("finalPrice", negotiation.getCurrentOffer(), "farmerId", user.getId()));

		Long recipientId = user.getId().equals(negotiation.getFarmerId()) ? negotiation.getDealerId()
				: negotiation.getFarmerId();
		String cropTitle = "crop";
		try {
			CropResponse c = crops.getCrop(negotiation.getCropId());
			if (c != null && c.getCropName() != null)
				cropTitle = c.getCropName();
		} catch (Exception ignored) {
		}

		OrderEvent acceptEvt = new OrderEvent();
		acceptEvt.setFarmerId(negotiation.getFarmerId());
		acceptEvt.setDealerId(negotiation.getDealerId());
		acceptEvt.setRecipientId(recipientId);
		acceptEvt.setCropId(negotiation.getCropId());
		acceptEvt.setStatus("NEGOTIATION_ACCEPTED");
		if (user.getId().equals(negotiation.getFarmerId())) {
			acceptEvt.setMessage("Farmer accepted your offer of ₹" + negotiation.getCurrentOffer() + "/qtl for "
					+ cropTitle + " (Negotiation #" + id + ")! You can now create the order.");
		} else {
			acceptEvt.setMessage("Dealer accepted your counter-offer of ₹" + negotiation.getCurrentOffer() + "/qtl for "
					+ cropTitle + " (Negotiation #" + id + ")! Order creation is now enabled.");
		}
		sendNegotiationEvent(acceptEvt);

		return map(negotiation);
	}

	public NegotiationResponse reject(Long id) {

		Negotiation negotiation = findNegotiation(id);

		UserResponse user = users.getByAuthUserId(SecurityUtils.getCurrentAuthUserId());

		if (user == null) {
			throw new AccessDeniedException("User not found");
		}

		if (!user.getId().equals(negotiation.getFarmerId()) && !user.getId().equals(negotiation.getDealerId())) {
			throw new AccessDeniedException("Only negotiation participants can reject");
		}

		ensureOpen(negotiation);

		negotiation.setStatus(NegotiationStatus.REJECTED);
		negotiation.setUpdatedAt(LocalDateTime.now());

		negotiations.save(negotiation);

		events.append("NEGOTIATION", String.valueOf(id), "OFFER_REJECTED", Map.of("actorId", user.getId()));

		Long rejectRecipientId = user.getId().equals(negotiation.getFarmerId()) ? negotiation.getDealerId()
				: negotiation.getFarmerId();
		OrderEvent rejectEvt = new OrderEvent();
		rejectEvt.setFarmerId(negotiation.getFarmerId());
		rejectEvt.setDealerId(negotiation.getDealerId());
		rejectEvt.setRecipientId(rejectRecipientId);
		rejectEvt.setCropId(negotiation.getCropId());
		rejectEvt.setStatus("NEGOTIATION_REJECTED");
		rejectEvt.setMessage("Negotiation #" + id + " was declined.");
		sendNegotiationEvent(rejectEvt);

		return map(negotiation);
	}

	private void sendNegotiationEvent(OrderEvent evt) {
		if (orderEventProducer != null) {
			try {
				orderEventProducer.publishNegotiationEvent(evt);
			} catch (Exception ex) {
				// Event publishing is non-blocking
			}
		}
	}

	public NegotiationResponse get(Long id) {

		Negotiation negotiation = findNegotiation(id);

		UserResponse user = users.getByAuthUserId(SecurityUtils.getCurrentAuthUserId());

		if (user == null) {
			throw new AccessDeniedException("User not found");
		}

		if (!SecurityUtils.hasRole("ADMIN") && !user.getId().equals(negotiation.getFarmerId())
				&& !user.getId().equals(negotiation.getDealerId())) {
			throw new AccessDeniedException("You are not a participant in this negotiation");
		}

		return map(negotiation);
	}

	public List<NegotiationResponse> getByDealer(Long dealerId) {
		return negotiations.findByDealerIdOrderByUpdatedAtDesc(dealerId).stream().map(this::map).toList();
	}

	public List<NegotiationResponse> getByFarmer(Long farmerId) {
		return negotiations.findByFarmerIdOrderByUpdatedAtDesc(farmerId).stream().map(this::map).toList();
	}

	public List<NegotiationResponse> history(Long id) {
		return List.of(get(id));
	}

	private Negotiation findNegotiation(Long id) {

		return negotiations.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Negotiation not found: " + id));
	}

	private void requireDealer(Negotiation negotiation, UserResponse user) {

		if (user == null || !user.getId().equals(negotiation.getDealerId())
				|| !"DEALER".equalsIgnoreCase(user.getRole())) {
			throw new AccessDeniedException("Only the negotiation dealer can act");
		}
	}

	private void requireFarmer(Negotiation negotiation, UserResponse user) {

		if (user == null || !user.getId().equals(negotiation.getFarmerId())
				|| !"FARMER".equalsIgnoreCase(user.getRole())) {
			throw new AccessDeniedException("Only the negotiation farmer can act");
		}
	}

	private void requireParticipant(Negotiation negotiation, UserResponse user) {
		if (user == null || (!user.getId().equals(negotiation.getFarmerId())
				&& !user.getId().equals(negotiation.getDealerId()))) {
			throw new AccessDeniedException("Only the farmer or dealer in this negotiation can act");
		}
		if (!"FARMER".equalsIgnoreCase(user.getRole()) && !"DEALER".equalsIgnoreCase(user.getRole())) {
			throw new AccessDeniedException("Only farmer or dealer can accept an offer");
		}
	}

	private void ensureOpen(Negotiation negotiation) {

		if (negotiation.getStatus() != NegotiationStatus.OPEN
				&& negotiation.getStatus() != NegotiationStatus.COUNTERED) {
			throw new IllegalArgumentException("Negotiation is not open");
		}
	}

	private void addOffer(Negotiation negotiation, String role, Long actor, Double amount, String status) {

		Offer offer = new Offer();

		offer.setNegotiationId(negotiation.getId());
		offer.setActorRole(role);
		offer.setActorId(actor);
		offer.setAmount(amount);
		offer.setStatus(status);
		offer.setCreatedAt(LocalDateTime.now());

		offers.save(offer);
	}

	private void markLastPendingAccepted(Negotiation negotiation) {

		List<Offer> offerList = offers.findByNegotiationIdOrderByCreatedAtAsc(negotiation.getId());

		for (int i = offerList.size() - 1; i >= 0; i--) {

			Offer offer = offerList.get(i);

			if (!"ASKING_PRICE".equals(offer.getStatus())) {
				offer.setStatus("ACCEPTED");
				offers.save(offer);
				break;
			}
		}
	}

	private NegotiationResponse map(Negotiation negotiation) {

		if (orderRepository != null && negotiation.getStatus() == NegotiationStatus.ACCEPTED) {
			Long ordId = negotiation.getOrderId();
			java.util.Optional<com.cropdeal.order.entity.Order> linkedOrder = ordId != null
					? orderRepository.findById(ordId)
					: orderRepository.findByNegotiationId(negotiation.getId());
			if (linkedOrder.isPresent()) {
				com.cropdeal.order.entity.Order ord = linkedOrder.get();
				boolean changed = false;
				if (negotiation.getOrderId() == null) {
					negotiation.setOrderId(ord.getId());
					negotiation.setOrderNumber(ord.getOrderNumber());
					changed = true;
				}
				if (ord.getStatus() == com.cropdeal.order.entity.OrderStatus.COMPLETED) {
					negotiation.setStatus(NegotiationStatus.COMPLETED);
					negotiation.setUpdatedAt(LocalDateTime.now());
					changed = true;
				}
				if (changed) {
					negotiation = negotiations.save(negotiation);
				}
			}
		}

		NegotiationResponse response = new NegotiationResponse();

		response.setId(negotiation.getId());
		response.setCropId(negotiation.getCropId());
		response.setQuantity(negotiation.getQuantity());
		response.setFarmerId(negotiation.getFarmerId());
		response.setDealerId(negotiation.getDealerId());
		response.setAskingPrice(negotiation.getAskingPrice());
		response.setCurrentOffer(negotiation.getCurrentOffer());
		response.setOrderId(negotiation.getOrderId());
		response.setOrderNumber(negotiation.getOrderNumber());
		response.setStatus(negotiation.getStatus());
		if (negotiation.getStatus() == NegotiationStatus.ACCEPTED
				|| negotiation.getStatus() == NegotiationStatus.REJECTED) {
			response.setNextAction("NONE");
		} else {
			List<Offer> offerList = offers.findByNegotiationIdOrderByCreatedAtAsc(negotiation.getId());
			if (offerList.isEmpty())
				response.setNextAction("DEALER");
			else {
				Offer lastOffer = offerList.get(offerList.size() - 1);
				response.setNextAction("FARMER".equalsIgnoreCase(lastOffer.getActorRole()) ? "DEALER" : "FARMER");
			}
		}
		response.setCreatedAt(negotiation.getCreatedAt());
		response.setUpdatedAt(negotiation.getUpdatedAt());
		response.setAcceptedAt(negotiation.getAcceptedAt());

		try {
			CropResponse crop = crops.getCrop(negotiation.getCropId());
			if (crop != null && crop.getCropName() != null) {
				response.setCropName(crop.getCropName());
			}
		} catch (Exception ignored) {
			response.setCropName("Crop #" + negotiation.getCropId());
		}

		try {
			UserResponse farmer = users.getUser(negotiation.getFarmerId());
			if (farmer != null && farmer.getName() != null) {
				response.setFarmerName(farmer.getName());
			}
		} catch (Exception ignored) {
			response.setFarmerName("Farmer #" + negotiation.getFarmerId());
		}

		try {
			UserResponse dealer = users.getUser(negotiation.getDealerId());
			if (dealer != null && dealer.getName() != null) {
				response.setDealerName(dealer.getName());
			}
		} catch (Exception ignored) {
			response.setDealerName("Dealer #" + negotiation.getDealerId());
		}

		response.setOffers(offers.findByNegotiationIdOrderByCreatedAtAsc(negotiation.getId()).stream()
				.map(this::mapOffer).toList());

		return response;
	}

	public List<NegotiationResponse> getAllNegotiations() {
		return negotiations.findAll().stream()
				.sorted((a, b) -> (b.getUpdatedAt() != null && a.getUpdatedAt() != null)
						? b.getUpdatedAt().compareTo(a.getUpdatedAt())
						: 0)
				.map(this::map).toList();
	}

	private OfferResponse mapOffer(Offer offer) {

		OfferResponse response = new OfferResponse();

		response.setId(offer.getId());
		response.setNegotiationId(offer.getNegotiationId());
		response.setActorId(offer.getActorId());
		response.setActorRole(offer.getActorRole());
		response.setAmount(offer.getAmount());
		response.setStatus(offer.getStatus());
		response.setCreatedAt(offer.getCreatedAt());

		return response;
	}
}