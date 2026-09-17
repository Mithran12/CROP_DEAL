package com.cropdeal.order.service;

import java.util.List;

import com.cropdeal.order.dto.request.CounterOfferRequestDto;
import com.cropdeal.order.dto.request.CreateOfferRequestDto;
import com.cropdeal.order.dto.response.NegotiationResponseDto;
import com.cropdeal.order.dto.response.OfferResponseDto;

public interface NegotiationService {

	NegotiationResponseDto createNegotiation(Long purchaseRequestId, Long authenticatedUserId);

	NegotiationResponseDto getNegotiationById(Long id, Long authenticatedUserId);

	List<OfferResponseDto> getOffers(Long negotiationId, Long authenticatedUserId);

	OfferResponseDto createOffer(Long negotiationId, CreateOfferRequestDto requestDto, Long userId, String role);

	OfferResponseDto createCounterOffer(Long negotiationId, CounterOfferRequestDto requestDto, Long userId,
			String role);

	NegotiationResponseDto acceptOffer(Long negotiationId, Long offerId, Long authenticatedUserId);

	NegotiationResponseDto rejectOffer(Long negotiationId, Long offerId, Long authenticatedUserId);
}