package com.cropdeal.order.service;

import java.util.List;

import com.cropdeal.order.dto.request.CounterOfferRequestDto;
import com.cropdeal.order.dto.request.CreateOfferRequestDto;
import com.cropdeal.order.dto.response.NegotiationResponseDto;
import com.cropdeal.order.dto.response.OfferResponseDto;

public interface NegotiationService {

    NegotiationResponseDto createNegotiation(Long purchaseRequestId);

    NegotiationResponseDto getNegotiationById(Long id);

    List<OfferResponseDto> getOffers(Long negotiationId);

    OfferResponseDto createOffer(
            Long negotiationId,
            CreateOfferRequestDto requestDto,
            Long userId,
            String role);

    OfferResponseDto createCounterOffer(
            Long negotiationId,
            CounterOfferRequestDto requestDto,
            Long userId,
            String role);

    NegotiationResponseDto acceptOffer(
            Long negotiationId,
            Long offerId);

    NegotiationResponseDto rejectOffer(
            Long negotiationId,
            Long offerId);
}