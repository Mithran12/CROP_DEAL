package com.cropdeal.auction.service;

import com.cropdeal.auction.client.CropClient;
import com.cropdeal.auction.client.NotificationClient;
import com.cropdeal.auction.client.OrderClient;
import com.cropdeal.auction.client.PricingClient;
import com.cropdeal.auction.client.SubscriptionClient;
import com.cropdeal.auction.client.UserClient;
import com.cropdeal.auction.client.WalletClient;

import com.cropdeal.auction.dto.AuctionResponse;
import com.cropdeal.auction.dto.BidResponse;
import com.cropdeal.auction.dto.CreateAuctionRequest;
import com.cropdeal.auction.dto.CreateNotificationRequest;
import com.cropdeal.auction.dto.CropResponse;
import com.cropdeal.auction.dto.OrderResponse;
import com.cropdeal.auction.dto.PlaceBidRequest;
import com.cropdeal.auction.dto.PricingResponse;
import com.cropdeal.auction.dto.UserResponse;
import com.cropdeal.auction.dto.WalletFundsRequest;

import com.cropdeal.auction.entity.Auction;
import com.cropdeal.auction.entity.AuctionStatus;
import com.cropdeal.auction.entity.Bid;

import com.cropdeal.auction.repository.AuctionRepository;
import com.cropdeal.auction.repository.BidRepository;

import com.cropdeal.auction.security.SecurityUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuctionServiceImpl implements AuctionService {

	@Value("${internal.api.key:change-this-internal-key}")
	private String internalApiKey;

	private final AuctionRepository auctionRepository;
	private final BidRepository bidRepository;

	private final CropClient cropClient;
	private final PricingClient pricingClient;
	private final WalletClient walletClient;
	private final UserClient userClient;
	private final NotificationClient notificationClient;
	private final SubscriptionClient subscriptionClient;
	private final OrderClient orderClient;

	private final SimpMessagingTemplate messagingTemplate;

	public AuctionServiceImpl(AuctionRepository auctionRepository, BidRepository bidRepository, CropClient cropClient,
			PricingClient pricingClient, WalletClient walletClient, UserClient userClient,
			NotificationClient notificationClient, SubscriptionClient subscriptionClient, OrderClient orderClient,
			SimpMessagingTemplate messagingTemplate) {

		this.auctionRepository = auctionRepository;
		this.bidRepository = bidRepository;
		this.cropClient = cropClient;
		this.pricingClient = pricingClient;
		this.walletClient = walletClient;
		this.userClient = userClient;
		this.notificationClient = notificationClient;
		this.subscriptionClient = subscriptionClient;
		this.orderClient = orderClient;
		this.messagingTemplate = messagingTemplate;
	}

	@Override
	public AuctionResponse createAuction(CreateAuctionRequest request) {

		CropResponse crop = cropClient.getCropById(request.getCropId());

		UserResponse farmer = userClient.getUserById(request.getFarmerId());

		if (!"FARMER".equalsIgnoreCase(farmer.getRole()) || !farmer.isActive()) {

			throw new IllegalArgumentException("Farmer account is not valid");
		}

		if (SecurityUtils.getCurrentAuthUserId() == null
				|| !SecurityUtils.getCurrentAuthUserId().equals(farmer.getAuthUserId())) {

			throw new AccessDeniedException("You can create auctions only for your own farmer profile");
		}

		if (!crop.getFarmerId().equals(request.getFarmerId())) {

			throw new IllegalArgumentException("This crop does not belong to the farmer");
		}

		if (!"AVAILABLE".equalsIgnoreCase(crop.getStatus())) {

			throw new IllegalArgumentException("Only available crops can be auctioned");
		}

		if (auctionRepository.existsByCropIdAndStatus(request.getCropId(), AuctionStatus.OPEN)) {

			throw new IllegalArgumentException("An open auction already exists for this crop");
		}

		if (crop.getGrade() == null || !crop.getGrade().equalsIgnoreCase(request.getGrade())) {

			throw new IllegalArgumentException("Auction grade must match the crop grade");
		}

		Double basePrice = crop.getExpectedPrice();

		try {

			Map<String, Object> pricingRequest = new HashMap<>();

			pricingRequest.put("cropName", crop.getCropName());

			pricingRequest.put("location", crop.getLocation());

			pricingRequest.put("grade", crop.getGrade());

			PricingResponse pricingResponse = pricingClient.calculatePrice(pricingRequest);

			if (pricingResponse != null && pricingResponse.getFairBasePrice() != null) {

				basePrice = pricingResponse.getFairBasePrice();
			}

		} catch (Exception ex) {

			if (basePrice == null || basePrice <= 0) {

				basePrice = 1000.0;
			}
		}

		LocalDateTime startTime = LocalDateTime.now();

		Auction auction = new Auction();

		auction.setCropId(crop.getId());

		auction.setFarmerId(crop.getFarmerId());

		auction.setBasePrice(basePrice);

		auction.setHighestBid(null);

		auction.setHighestBidderId(null);

		auction.setStartTime(startTime);

		auction.setEndTime(startTime.plusMinutes(request.getDurationMinutes()));

		auction.setStatus(AuctionStatus.OPEN);

		auction.setCreatedAt(LocalDateTime.now());

		auction.setUpdatedAt(LocalDateTime.now());

		Auction savedAuction = auctionRepository.save(auction);

		notifyAuctionStarted(savedAuction);

		return mapAuctionToResponse(savedAuction);
	}

	@Override
	public BidResponse placeBid(Long auctionId, PlaceBidRequest request) {

		Auction auction = auctionRepository.findById(auctionId)
				.orElseThrow(() -> new IllegalArgumentException("Auction not found"));

		UserResponse dealer = userClient.getUserById(request.getDealerId());

		if (!"DEALER".equalsIgnoreCase(dealer.getRole()) || !dealer.isActive()) {

			throw new IllegalArgumentException("Dealer account is not valid");
		}

		if (SecurityUtils.getCurrentAuthUserId() == null
				|| !SecurityUtils.getCurrentAuthUserId().equals(dealer.getAuthUserId())) {

			throw new AccessDeniedException("You can place bids only for your own dealer profile");
		}

		if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.ACTIVE) {

			throw new IllegalArgumentException("Auction is not currently open for bidding");
		}

		if (LocalDateTime.now().isAfter(auction.getEndTime())) {

			closeAuction(auction);

			throw new IllegalArgumentException("Auction has ended and is awaiting order creation");
		}

		double currentHighestPrice = auction.getHighestBid() == null ? auction.getBasePrice() : auction.getHighestBid();

		if (request.getAmount() <= currentHighestPrice) {

			throw new IllegalArgumentException("Bid must be greater than current highest price");
		}

		CropResponse crop = cropClient.getCropById(auction.getCropId());

		double quantity = crop.getQuantity();

		double newTotalAmount = request.getAmount() * quantity;

		List<Bid> existingBids = bidRepository.findByAuctionId(auctionId);

		double previousDealerMaxBid = existingBids.stream().filter(b -> b.getDealerId().equals(request.getDealerId()))
				.mapToDouble(Bid::getAmount).max().orElse(0.0);

		double previousTotalAmount = previousDealerMaxBid * quantity;

		double amountToReserve = newTotalAmount - previousTotalAmount;

		if (amountToReserve > 0) {

			reserveFunds(request.getDealerId(), amountToReserve, auctionId);
		}

		Bid savedBid;

		try {

			Bid bid = new Bid();

			bid.setAuctionId(auctionId);

			bid.setDealerId(request.getDealerId());

			bid.setAmount(request.getAmount());

			bid.setBidTime(LocalDateTime.now());

			savedBid = bidRepository.save(bid);

			auction.setHighestBid(request.getAmount());

			auction.setHighestBidderId(request.getDealerId());

			auction.setUpdatedAt(LocalDateTime.now());

			auctionRepository.save(auction);

		} catch (RuntimeException ex) {

			if (amountToReserve > 0) {

				try {

					releaseFunds(request.getDealerId(), amountToReserve, auctionId);

				} catch (Exception ignored) {
				}
			}

			throw ex;
		}

		BidResponse response = mapBidToResponse(savedBid);

		messagingTemplate.convertAndSend("/topic/auctions/" + auctionId, response);

		return response;
	}

	@Override
	public AuctionResponse getAuctionById(Long auctionId) {

		synchronizeAuctionStates();

		Auction auction = auctionRepository.findById(auctionId)
				.orElseThrow(() -> new IllegalArgumentException("Auction not found"));

		return mapAuctionToResponse(auction);
	}

	@Override
	public List<AuctionResponse> getOpenAuctions() {

		synchronizeAuctionStates();

		return auctionRepository
				.findByStatusInOrderByCreatedAtDesc(
						List.of(AuctionStatus.SCHEDULED, AuctionStatus.ACTIVE, AuctionStatus.OPEN, AuctionStatus.ENDED))
				.stream().map(this::mapAuctionToResponse).filter(a -> a.getStatus() != AuctionStatus.COMPLETED
						&& a.getStatus() != AuctionStatus.CANCELLED && a.getStatus() != AuctionStatus.EXPIRED)
				.toList();
	}

	@Override
	public List<AuctionResponse> getAuctionHistory() {

		synchronizeAuctionStates();

		return auctionRepository
				.findByStatusInOrderByEndTimeDesc(
						List.of(AuctionStatus.COMPLETED, AuctionStatus.EXPIRED, AuctionStatus.CANCELLED))
				.stream().map(this::mapAuctionToResponse).toList();
	}

	@Override
	public List<AuctionResponse> getAuctionsByFarmer(Long farmerId) {

		synchronizeAuctionStates();

		return auctionRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId).stream().map(this::mapAuctionToResponse)
				.toList();
	}

	@Override
	public List<AuctionResponse> getAuctionsByDealer(Long dealerId) {

		synchronizeAuctionStates();

		List<Bid> bids = bidRepository.findByDealerId(dealerId);

		Set<Long> auctionIds = bids.stream().map(Bid::getAuctionId).collect(Collectors.toSet());

		List<Auction> wonAuctions = auctionRepository.findByHighestBidderIdOrderByEndTimeDesc(dealerId);

		wonAuctions.forEach(a -> auctionIds.add(a.getId()));

		if (auctionIds.isEmpty()) {
			return List.of();
		}

		return auctionRepository.findAllById(auctionIds).stream()
				.sorted((a, b) -> (b.getEndTime() != null && a.getEndTime() != null)
						? b.getEndTime().compareTo(a.getEndTime())
						: 0)
				.map(this::mapAuctionToResponse).toList();
	}

	@Override
	public List<AuctionResponse> getAllAuctions() {

		synchronizeAuctionStates();

		return auctionRepository.findAll().stream()
				.sorted((a, b) -> (b.getCreatedAt() != null && a.getCreatedAt() != null)
						? b.getCreatedAt().compareTo(a.getCreatedAt())
						: 0)
				.map(this::mapAuctionToResponse).toList();
	}

	@Override
	public AuctionResponse completeAuction(Long auctionId) {

		Auction auction = auctionRepository.findById(auctionId)
				.orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

		auction.setStatus(AuctionStatus.COMPLETED);

		auction.setPaymentStatus("PAID");

		auction.setDeliveryStatus("DELIVERED");

		auction.setUpdatedAt(LocalDateTime.now());

		Auction saved = auctionRepository.save(auction);

		return mapAuctionToResponse(saved);
	}

	@Override
	public AuctionResponse linkOrder(Long auctionId, Long orderId, String orderNumber) {

		Auction auction = auctionRepository.findById(auctionId)
				.orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

		auction.setOrderId(orderId);

		auction.setOrderNumber(orderNumber);

		auction.setPaymentStatus("PENDING");

		auction.setDeliveryStatus("PENDING");

		auction.setUpdatedAt(LocalDateTime.now());

		Auction saved = auctionRepository.save(auction);

		return mapAuctionToResponse(saved);
	}

	@Override
	public void synchronizeAuctionStates() {

		LocalDateTime now = LocalDateTime.now();

		List<Auction> scheduled = auctionRepository.findByStatus(AuctionStatus.SCHEDULED);

		for (Auction a : scheduled) {

			if (!now.isBefore(a.getStartTime()) && now.isBefore(a.getEndTime())) {

				a.setStatus(AuctionStatus.OPEN);

				a.setUpdatedAt(now);

				auctionRepository.save(a);

				notifyAuctionStarted(a);

			} else if (now.isAfter(a.getEndTime())) {

				try {

					closeAuction(a);

				} catch (Exception e) {

					System.err.println("Could not close scheduled auction " + a.getId() + ": " + e.getMessage());
				}
			}
		}

		List<Auction> activeAuctions = auctionRepository
				.findByStatusIn(List.of(AuctionStatus.OPEN, AuctionStatus.ACTIVE));

		for (Auction auction : activeAuctions) {

			if (now.isAfter(auction.getEndTime())) {

				try {

					closeAuction(auction);

				} catch (Exception e) {

					System.err.println("Could not close auction " + auction.getId() + ": " + e.getMessage());
				}
			}
		}
	}

	public void closeAuction(Auction auction) {

		if (auction.getStatus() == AuctionStatus.ENDED || auction.getStatus() == AuctionStatus.COMPLETED
				|| auction.getStatus() == AuctionStatus.EXPIRED) {

			return;
		}

		CropResponse crop = null;

		try {

			crop = cropClient.getCropById(auction.getCropId());

		} catch (Exception ex) {

			System.err.println("Could not fetch crop for auction " + auction.getId() + ": " + ex.getMessage());
		}

		double cropQuantity = (crop != null && crop.getQuantity() != null) ? crop.getQuantity() : 1.0;

		String cropName = (crop != null && crop.getCropName() != null) ? crop.getCropName() : "Harvest";

		if (auction.getHighestBidderId() != null) {

			auction.setStatus(AuctionStatus.ENDED);

			List<Bid> bids = bidRepository.findByAuctionId(auction.getId());

			Long winnerId = auction.getHighestBidderId();

			Map<Long, Double> loserMaxBids = new HashMap<>();

			for (Bid b : bids) {

				if (!b.getDealerId().equals(winnerId)) {

					loserMaxBids.merge(b.getDealerId(), b.getAmount(), Math::max);
				}
			}

			for (Map.Entry<Long, Double> entry : loserMaxBids.entrySet()) {

				Long loserDealerId = entry.getKey();

				Double maxBidAmount = entry.getValue();

				double refundTotal = maxBidAmount * cropQuantity;

				try {

					WalletFundsRequest releaseReq = new WalletFundsRequest();

					releaseReq.setAmount(refundTotal);

					releaseReq.setReference("AUCTION-REFUND-" + auction.getId());

					walletClient.releaseFundsInternal(loserDealerId, internalApiKey, releaseReq);

				} catch (Exception e) {

					System.err.println(
							"Could not release funds for losing dealer " + loserDealerId + ": " + e.getMessage());
				}
			}

			UserResponse winnerUser = null;

			try {

				winnerUser = userClient.getUserById(winnerId);

			} catch (Exception ignored) {
			}

			String winnerName = (winnerUser != null && winnerUser.getName() != null) ? winnerUser.getName() : "Dealer";

			try {

				if (auction.getFarmerId() != null) {

					notificationClient.createNotification(internalApiKey,
							new CreateNotificationRequest(auction.getFarmerId(), "AUCTION_ENDED",
									"Your " + cropName + " auction has ended with a winning bid of ₹"
											+ auction.getHighestBid() + " from " + winnerName + "."));
				}

				for (Long loserDealerId : loserMaxBids.keySet()) {

					try {

						notificationClient.createNotification(internalApiKey,
								new CreateNotificationRequest(loserDealerId, "AUCTION_ENDED", "The auction for "
										+ cropName + " has ended. The winning bid was ₹" + auction.getHighestBid()
										+ "/qtl. Your reserved funds have been returned to your wallet balance."));
					} catch (Exception ignored) {
					}
				}

			} catch (Exception ex) {

				System.err.println("Could not send AUCTION_ENDED notification: " + ex.getMessage());
			}

			try {

				String winMessage = "Congratulations " + winnerName + "! You won the " + cropName
						+ " auction with winning bid of ₹" + auction.getHighestBid()
						+ "/qtl. Please click 'Create Order' to proceed with order fulfillment.";

				notificationClient.createNotification(internalApiKey,
						new CreateNotificationRequest(winnerId, "AUCTION_WON", winMessage));

			} catch (Exception ex) {

				System.err.println("Could not send AUCTION_WON notification: " + ex.getMessage());
			}

		} else {

			auction.setStatus(AuctionStatus.EXPIRED);

			try {

				if (auction.getFarmerId() != null) {

					notificationClient.createNotification(internalApiKey,
							new CreateNotificationRequest(auction.getFarmerId(), "AUCTION_EXPIRED",
									"Your auction for " + cropName + " expired with no bids placed."));
				}

			} catch (Exception ignored) {
			}
		}

		auction.setUpdatedAt(LocalDateTime.now());

		auctionRepository.save(auction);
	}

	public void notifyAuctionStarted(Auction auction) {

		if (auction == null || Boolean.TRUE.equals(auction.getStartNotified())) {

			return;
		}

		if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.ACTIVE) {

			return;
		}

		try {

			CropResponse crop = cropClient.getCropById(auction.getCropId());

			UserResponse farmer = userClient.getUserById(auction.getFarmerId());

			String farmerName = (farmer != null && farmer.getName() != null) ? farmer.getName() : "Farmer";

			String cropName = (crop != null && crop.getCropName() != null) ? crop.getCropName() : "Harvest";

			String category = (crop != null && crop.getCategory() != null) ? crop.getCategory() : null;

			String location = (crop != null && crop.getLocation() != null) ? crop.getLocation() : "the farm";

			Double quantity = (crop != null && crop.getQuantity() != null) ? crop.getQuantity() : 10.0;

			Double basePrice = auction.getBasePrice();

			List<Long> matchingDealerIds = null;

			try {

				matchingDealerIds = subscriptionClient.getMatchingDealers(cropName, category, location, quantity,
						basePrice);

			} catch (Exception ex) {

				System.err.println("Could not query subscription service: " + ex.getMessage());
			}

			if (matchingDealerIds != null && !matchingDealerIds.isEmpty()) {

				String message = "Auction started for " + cropName + " (" + quantity + " qtl) by " + farmerName + " at "
						+ location + ". Floor price: ₹" + basePrice + "/qtl. Auction ends at " + auction.getEndTime()
						+ ". Join now to place your bid!";

				for (Long dealerId : matchingDealerIds) {

					if (dealerId == null || dealerId.equals(auction.getFarmerId())) {

						continue;
					}

					try {

						UserResponse dealer = userClient.getUserById(dealerId);

						if (dealer != null && "DEALER".equalsIgnoreCase(dealer.getRole()) && dealer.isActive()) {

							notificationClient.createNotification(internalApiKey,
									new CreateNotificationRequest(dealerId, "AUCTION_STARTED", message));
						}

					} catch (Exception ex) {

						System.err.println("Could not notify dealer " + dealerId + ": " + ex.getMessage());
					}
				}
			}

			auction.setStartNotified(true);

			auctionRepository.save(auction);

		} catch (Exception ex) {

			System.err
					.println("Error notifying auction started for auction " + auction.getId() + ": " + ex.getMessage());
		}
	}

	@Override
	public List<BidResponse> getBidsByAuction(Long auctionId) {

		return bidRepository.findByAuctionId(auctionId).stream().map(this::mapBidToResponse).toList();
	}

	private void reserveFunds(Long dealerId, Double amount, Long auctionId) {

		WalletFundsRequest request = new WalletFundsRequest();

		request.setAmount(amount);

		request.setReference("AUCTION-" + auctionId);

		walletClient.reserveFunds(dealerId, request);
	}

	// ============================================================
	// RELEASE FUNDS
	// ============================================================

	private void releaseFunds(Long dealerId, Double amount, Long auctionId) {

		WalletFundsRequest request = new WalletFundsRequest();

		request.setAmount(amount);

		request.setReference("AUCTION-" + auctionId);

		walletClient.releaseFunds(dealerId, request);
	}

	// ============================================================
	// MAP AUCTION RESPONSE
	// ============================================================

	private AuctionResponse mapAuctionToResponse(Auction auction) {

		AuctionResponse response = new AuctionResponse();

		response.setId(auction.getId());

		response.setCropId(auction.getCropId());

		response.setFarmerId(auction.getFarmerId());

		response.setBasePrice(auction.getBasePrice());

		response.setHighestBid(auction.getHighestBid());

		response.setHighestBidderId(auction.getHighestBidderId());

		response.setStartTime(auction.getStartTime());

		response.setEndTime(auction.getEndTime());

		response.setStatus(auction.getStatus());

		// --------------------------------------------------------
		// Enrich with Crop Information
		// --------------------------------------------------------

		try {

			CropResponse crop = cropClient.getCropById(auction.getCropId());

			if (crop != null && crop.getCropName() != null) {

				response.setCropName(crop.getCropName());

				response.setLocation(crop.getLocation());

				response.setQuantity(crop.getQuantity());

			} else {

				response.setCropName("Deleted Crop");
			}

		} catch (Exception e) {

			response.setCropName("Deleted Crop");
		}

		try {

			UserResponse farmer = userClient.getUserById(auction.getFarmerId());

			if (farmer != null && farmer.getName() != null) {

				response.setFarmerName(farmer.getName());

			} else {

				response.setFarmerName("Farmer");
			}

		} catch (Exception e) {

			response.setFarmerName("Farmer");
		}

		if (auction.getHighestBidderId() != null) {

			try {

				UserResponse winner = userClient.getUserById(auction.getHighestBidderId());

				if (winner != null && winner.getName() != null) {

					response.setWinnerName(winner.getName());

				} else {

					response.setWinnerName("Dealer");
				}

			} catch (Exception e) {

				response.setWinnerName("Dealer");
			}

		} else {

			response.setWinnerName("No bids");
		}

		try {

			List<Bid> bids = bidRepository.findByAuctionId(auction.getId());

			response.setTotalBids(bids != null ? bids.size() : 0);

		} catch (Exception e) {

			response.setTotalBids(0);
		}

		if (auction.getOrderId() == null
				&& (auction.getStatus() == AuctionStatus.ENDED || auction.getStatus() == AuctionStatus.COMPLETED)) {

			try {

				OrderResponse linkedOrder = orderClient.getOrderByAuctionId(auction.getId());

				if (linkedOrder != null && linkedOrder.getId() != null) {

					auction.setOrderId(linkedOrder.getId());

					auction.setOrderNumber(linkedOrder.getOrderNumber());

					auction.setPaymentStatus(linkedOrder.getPaymentStatus());

					auction.setDeliveryStatus(linkedOrder.getPickupStatus());

					if ("COMPLETED".equalsIgnoreCase(linkedOrder.getStatus())) {

						auction.setStatus(AuctionStatus.COMPLETED);
					}

					auction = auctionRepository.save(auction);
				}

			} catch (Exception ignored) {
			}

		} else if (auction.getOrderId() != null && auction.getStatus() == AuctionStatus.ENDED) {

			try {

				OrderResponse linkedOrder = orderClient.getOrderByAuctionId(auction.getId());

				if (linkedOrder != null) {

					auction.setPaymentStatus(linkedOrder.getPaymentStatus());

					auction.setDeliveryStatus(linkedOrder.getPickupStatus());

					if ("COMPLETED".equalsIgnoreCase(linkedOrder.getStatus())) {

						auction.setStatus(AuctionStatus.COMPLETED);

						auction = auctionRepository.save(auction);
					}
				}

			} catch (Exception ignored) {
			}
		}

		response.setOrderId(auction.getOrderId());

		response.setOrderNumber(auction.getOrderNumber());

		response.setPaymentStatus(auction.getPaymentStatus());

		response.setDeliveryStatus(auction.getDeliveryStatus());

		response.setStatus(auction.getStatus());

		response.setCompletedAt(auction.getUpdatedAt());

		if (response.getWinnerName() != null && !response.getWinnerName().equals("No bids")) {

			response.setWinningDealer(response.getWinnerName());
		}

		return response;
	}

	private BidResponse mapBidToResponse(Bid bid) {

		BidResponse response = new BidResponse();

		response.setId(bid.getId());

		response.setAuctionId(bid.getAuctionId());

		response.setDealerId(bid.getDealerId());

		response.setAmount(bid.getAmount());

		response.setBidTime(bid.getBidTime());

		try {

			UserResponse dealer = userClient.getUserById(bid.getDealerId());

			if (dealer != null && dealer.getName() != null) {

				response.setDealerName(dealer.getName());

			} else {

				response.setDealerName("Dealer");
			}

		} catch (Exception e) {

			response.setDealerName("Dealer");
		}

		return response;
	}
}