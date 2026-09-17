package com.cropdeal.order.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cropdeal.order.dto.request.CounterOfferRequestDto;
import com.cropdeal.order.dto.request.CreateOfferRequestDto;
import com.cropdeal.order.dto.response.NegotiationResponseDto;
import com.cropdeal.order.dto.response.OfferResponseDto;
import com.cropdeal.order.entity.Negotiation;
import com.cropdeal.order.entity.NegotiationOffer;
import com.cropdeal.order.entity.PurchaseRequest;
import com.cropdeal.order.enums.NegotiationStatus;
import com.cropdeal.order.enums.OfferStatus;
import com.cropdeal.order.exception.BusinessException;
import com.cropdeal.order.exception.ResourceNotFoundException;
import com.cropdeal.order.exception.UnauthorizedResourceException;
import com.cropdeal.order.repository.NegotiationOfferRepository;
import com.cropdeal.order.repository.NegotiationRepository;
import com.cropdeal.order.repository.PurchaseRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NegotiationServiceImpl implements NegotiationService {

	private final NegotiationRepository negotiationRepository;

	private final NegotiationOfferRepository negotiationOfferRepository;

	private final PurchaseRequestRepository purchaseRequestRepository;

	// 1. CREATE NEGOTIATION

	@Override
	@Transactional
	public NegotiationResponseDto createNegotiation(Long purchaseRequestId, Long authenticatedUserId) {

		log.info("Creating negotiation for purchaseRequestId={}, userId={}", purchaseRequestId, authenticatedUserId);

		PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(purchaseRequestId).orElseThrow(
				() -> new ResourceNotFoundException("Purchase request not found with id: " + purchaseRequestId));

		validatePurchaseRequestParticipant(purchaseRequest, authenticatedUserId);

		Negotiation negotiation = new Negotiation();

		negotiation.setPurchaseRequestId(purchaseRequest.getId());

		negotiation.setCropId(purchaseRequest.getCropId());

		negotiation.setFarmerId(purchaseRequest.getFarmerId());

		negotiation.setDealerId(purchaseRequest.getDealerId());

		negotiation.setNegotiationNumber("NEG-" + System.currentTimeMillis());

		negotiation.setStatus(NegotiationStatus.ACTIVE);

		negotiation.setFinalPrice(null);

		LocalDateTime now = LocalDateTime.now();

		negotiation.setCreatedAt(now);
		negotiation.setUpdatedAt(now);

		Negotiation savedNegotiation = negotiationRepository.save(negotiation);

		log.info("Negotiation created successfully. negotiationId={}, purchaseRequestId={}", savedNegotiation.getId(),
				purchaseRequestId);

		return mapToNegotiationResponseDto(savedNegotiation);
	}

	// 2. GET NEGOTIATION

	@Override
	@Transactional(readOnly = true)
	public NegotiationResponseDto getNegotiationById(Long id, Long authenticatedUserId) {

		Negotiation negotiation = findNegotiationById(id);

		validateParticipantAccess(negotiation, authenticatedUserId);

		return mapToNegotiationResponseDto(negotiation);
	}

	// 3. GET OFFERS

	@Override
	@Transactional(readOnly = true)
	public List<OfferResponseDto> getOffers(Long negotiationId, Long authenticatedUserId) {

		Negotiation negotiation = findNegotiationById(negotiationId);

		validateParticipantAccess(negotiation, authenticatedUserId);

		return negotiationOfferRepository.findByNegotiationIdOrderByCreatedAtAsc(negotiationId).stream()
				.map(this::mapToOfferResponseDto).toList();
	}

	// 4. CREATE OFFER

	@Override
	@Transactional
	public OfferResponseDto createOffer(Long negotiationId, CreateOfferRequestDto requestDto, Long userId,
			String role) {

		Negotiation negotiation = findNegotiationById(negotiationId);

		validateParticipantAccess(negotiation, userId);

		validateRoleMatchesParticipant(negotiation, userId, role);

		if (negotiation.getStatus() != NegotiationStatus.ACTIVE) {

			throw new BusinessException("Cannot create offer for inactive negotiation");
		}

		NegotiationOffer offer = new NegotiationOffer();

		offer.setNegotiationId(negotiationId);

		offer.setOfferedByUserId(userId);

		offer.setOfferedByRole(role);

		offer.setPrice(requestDto.getPrice());

		offer.setQuantity(requestDto.getQuantity());

		offer.setMessage(requestDto.getMessage());

		offer.setOfferStatus(OfferStatus.PENDING);

		offer.setCreatedAt(LocalDateTime.now());

		NegotiationOffer savedOffer = negotiationOfferRepository.save(offer);

		negotiation.setUpdatedAt(LocalDateTime.now());

		negotiationRepository.save(negotiation);

		log.info("Offer created. negotiationId={}, offerId={}, userId={}, role={}", negotiationId, savedOffer.getId(),
				userId, role);

		return mapToOfferResponseDto(savedOffer);
	}

	// 5. CREATE COUNTER OFFER

	@Override
	@Transactional
	public OfferResponseDto createCounterOffer(Long negotiationId, CounterOfferRequestDto requestDto, Long userId,
			String role) {

		Negotiation negotiation = findNegotiationById(negotiationId);

		validateParticipantAccess(negotiation, userId);

		validateRoleMatchesParticipant(negotiation, userId, role);

		if (negotiation.getStatus() != NegotiationStatus.ACTIVE) {

			throw new BusinessException("Cannot create counter-offer for inactive negotiation");
		}

		List<NegotiationOffer> existingOffers = negotiationOfferRepository
				.findByNegotiationIdOrderByCreatedAtAsc(negotiationId);

		if (!existingOffers.isEmpty()) {

			NegotiationOffer lastOffer = existingOffers.get(existingOffers.size() - 1);

			if (lastOffer.getOfferStatus() == OfferStatus.PENDING) {

				lastOffer.setOfferStatus(OfferStatus.COUNTERED);

				negotiationOfferRepository.save(lastOffer);
			}
		}

		NegotiationOffer counterOffer = new NegotiationOffer();

		counterOffer.setNegotiationId(negotiationId);

		counterOffer.setOfferedByUserId(userId);

		counterOffer.setOfferedByRole(role);

		counterOffer.setPrice(requestDto.getPrice());

		counterOffer.setQuantity(requestDto.getQuantity());

		counterOffer.setMessage(requestDto.getMessage());

		counterOffer.setOfferStatus(OfferStatus.PENDING);

		counterOffer.setCreatedAt(LocalDateTime.now());

		NegotiationOffer savedCounterOffer = negotiationOfferRepository.save(counterOffer);

		negotiation.setUpdatedAt(LocalDateTime.now());

		negotiationRepository.save(negotiation);

		log.info("Counter offer created. negotiationId={}, offerId={}, userId={}, role={}", negotiationId,
				savedCounterOffer.getId(), userId, role);

		return mapToOfferResponseDto(savedCounterOffer);
	}

	// 6. ACCEPT OFFER

	@Override
	@Transactional
	public NegotiationResponseDto acceptOffer(Long negotiationId, Long offerId, Long authenticatedUserId) {

		Negotiation negotiation = findNegotiationById(negotiationId);

		validateParticipantAccess(negotiation, authenticatedUserId);

		NegotiationOffer offer = negotiationOfferRepository.findById(offerId)
				.orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));

		if (!offer.getNegotiationId().equals(negotiationId)) {

			throw new BusinessException("Offer does not belong to this negotiation");
		}

		if (negotiation.getStatus() != NegotiationStatus.ACTIVE) {

			throw new BusinessException("Negotiation is not active");
		}

		if (offer.getOfferStatus() != OfferStatus.PENDING) {

			throw new BusinessException("Only pending offers can be accepted");
		}

		offer.setOfferStatus(OfferStatus.ACCEPTED);

		negotiationOfferRepository.save(offer);

		// Reject all other pending offers
		List<NegotiationOffer> offers = negotiationOfferRepository
				.findByNegotiationIdOrderByCreatedAtAsc(negotiationId);

		for (NegotiationOffer existingOffer : offers) {

			if (!existingOffer.getId().equals(offerId) && existingOffer.getOfferStatus() == OfferStatus.PENDING) {

				existingOffer.setOfferStatus(OfferStatus.REJECTED);

				negotiationOfferRepository.save(existingOffer);
			}
		}

		negotiation.setStatus(NegotiationStatus.ACCEPTED);

		negotiation.setFinalPrice(offer.getPrice());

		negotiation.setUpdatedAt(LocalDateTime.now());

		Negotiation updatedNegotiation = negotiationRepository.save(negotiation);

		log.info("Offer accepted. negotiationId={}, offerId={}, acceptedBy={}", negotiationId, offerId,
				authenticatedUserId);

		return mapToNegotiationResponseDto(updatedNegotiation);
	}

	// 7. REJECT OFFER

	@Override
	@Transactional
	public NegotiationResponseDto rejectOffer(Long negotiationId, Long offerId, Long authenticatedUserId) {

		Negotiation negotiation = findNegotiationById(negotiationId);

		validateParticipantAccess(negotiation, authenticatedUserId);

		NegotiationOffer offer = negotiationOfferRepository.findById(offerId)
				.orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));

		if (!offer.getNegotiationId().equals(negotiationId)) {

			throw new BusinessException("Offer does not belong to this negotiation");
		}

		if (negotiation.getStatus() != NegotiationStatus.ACTIVE) {

			throw new BusinessException("Negotiation is not active");
		}

		if (offer.getOfferStatus() != OfferStatus.PENDING) {

			throw new BusinessException("Only pending offers can be rejected");
		}

		offer.setOfferStatus(OfferStatus.REJECTED);

		negotiationOfferRepository.save(offer);

		negotiation.setUpdatedAt(LocalDateTime.now());

		Negotiation updatedNegotiation = negotiationRepository.save(negotiation);

		log.info("Offer rejected. negotiationId={}, offerId={}, rejectedBy={}", negotiationId, offerId,
				authenticatedUserId);

		return mapToNegotiationResponseDto(updatedNegotiation);
	}

	// AUTHORIZATION

	private void validatePurchaseRequestParticipant(PurchaseRequest purchaseRequest, Long authenticatedUserId) {

		boolean isFarmer = authenticatedUserId != null && authenticatedUserId.equals(purchaseRequest.getFarmerId());

		boolean isDealer = authenticatedUserId != null && authenticatedUserId.equals(purchaseRequest.getDealerId());

		if (!isFarmer && !isDealer) {

			throw new UnauthorizedResourceException("You are not authorized to access this purchase request");
		}
	}

	private void validateParticipantAccess(Negotiation negotiation, Long authenticatedUserId) {

		boolean isFarmer = authenticatedUserId != null && authenticatedUserId.equals(negotiation.getFarmerId());

		boolean isDealer = authenticatedUserId != null && authenticatedUserId.equals(negotiation.getDealerId());

		if (!isFarmer && !isDealer) {

			throw new UnauthorizedResourceException("You are not authorized to access this negotiation");
		}
	}

	private void validateRoleMatchesParticipant(Negotiation negotiation, Long userId, String role) {

		if (role == null) {

			throw new UnauthorizedResourceException("User role is required");
		}

		if ("FARMER".equalsIgnoreCase(role)) {

			if (!userId.equals(negotiation.getFarmerId())) {

				throw new UnauthorizedResourceException("Farmer is not associated with this negotiation");
			}

			return;
		}

		if ("DEALER".equalsIgnoreCase(role)) {

			if (!userId.equals(negotiation.getDealerId())) {

				throw new UnauthorizedResourceException("Dealer is not associated with this negotiation");
			}

			return;
		}

		throw new UnauthorizedResourceException("Invalid user role");
	}

	// FIND NEGOTIATION

	private Negotiation findNegotiationById(Long id) {

		return negotiationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Negotiation not found with id: " + id));
	}

	// ENTITY -> NEGOTIATION RESPONSE DTO

	private NegotiationResponseDto mapToNegotiationResponseDto(Negotiation negotiation) {

		return NegotiationResponseDto.builder().id(negotiation.getId())
				.negotiationNumber(negotiation.getNegotiationNumber())
				.purchaseRequestId(negotiation.getPurchaseRequestId()).cropId(negotiation.getCropId())
				.farmerId(negotiation.getFarmerId()).dealerId(negotiation.getDealerId()).status(negotiation.getStatus())
				.finalPrice(negotiation.getFinalPrice()).createdAt(negotiation.getCreatedAt())
				.updatedAt(negotiation.getUpdatedAt()).build();
	}

	// ENTITY -> OFFER RESPONSE DTO

	private OfferResponseDto mapToOfferResponseDto(NegotiationOffer offer) {

		return OfferResponseDto.builder().id(offer.getId()).negotiationId(offer.getNegotiationId())
				.offeredByUserId(offer.getOfferedByUserId()).offeredByRole(offer.getOfferedByRole())
				.price(offer.getPrice()).quantity(offer.getQuantity()).message(offer.getMessage())
				.offerStatus(offer.getOfferStatus()).createdAt(offer.getCreatedAt()).build();
	}
}