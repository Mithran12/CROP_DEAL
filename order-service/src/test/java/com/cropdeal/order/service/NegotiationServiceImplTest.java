package com.cropdeal.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cropdeal.order.dto.request.CounterOfferRequestDto;
import com.cropdeal.order.dto.request.CreateOfferRequestDto;
import com.cropdeal.order.dto.response.NegotiationResponseDto;
import com.cropdeal.order.dto.response.OfferResponseDto;
import com.cropdeal.order.entity.Negotiation;
import com.cropdeal.order.entity.NegotiationOffer;
import com.cropdeal.order.entity.PurchaseRequest;
import com.cropdeal.order.enums.NegotiationStatus;
import com.cropdeal.order.enums.OfferStatus;
import com.cropdeal.order.enums.PurchaseRequestStatus;
import com.cropdeal.order.repository.NegotiationOfferRepository;
import com.cropdeal.order.repository.NegotiationRepository;
import com.cropdeal.order.repository.PurchaseRequestRepository;

@ExtendWith(MockitoExtension.class)
class NegotiationServiceImplTest {

    @Mock
    private PurchaseRequestRepository purchaseRequestRepository;

    @Mock
    private NegotiationRepository negotiationRepository;

    @Mock
    private NegotiationOfferRepository negotiationOfferRepository;

    @InjectMocks
    private NegotiationServiceImpl negotiationService;

    @Test
    void createNegotiation_shouldCreateSuccessfully() {

        PurchaseRequest purchaseRequest =
                createPurchaseRequest();

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(purchaseRequest));

        when(negotiationRepository.save(any(Negotiation.class)))
                .thenAnswer(invocation -> {
                    Negotiation negotiation =
                            invocation.getArgument(0);

                    negotiation.setId(1L);

                    return negotiation;
                });

        NegotiationResponseDto response =
                negotiationService.createNegotiation(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getPurchaseRequestId());
        assertEquals(101L, response.getCropId());
        assertEquals(201L, response.getFarmerId());
        assertEquals(301L, response.getDealerId());
        assertEquals(
                NegotiationStatus.ACTIVE,
                response.getStatus());
        assertNull(response.getFinalPrice());

        verify(purchaseRequestRepository, times(1))
                .findById(1L);

        verify(negotiationRepository, times(1))
                .save(any(Negotiation.class));
    }

    @Test
    void createNegotiation_shouldThrowExceptionWhenPurchaseRequestNotFound() {

        when(purchaseRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> negotiationService
                        .createNegotiation(999L));

        verify(negotiationRepository, never())
                .save(any(Negotiation.class));
    }

    @Test
    void getNegotiationById_shouldReturnSuccessfully() {

        Negotiation negotiation =
                createNegotiation(
                        1L,
                        NegotiationStatus.ACTIVE);

        when(negotiationRepository.findById(1L))
                .thenReturn(Optional.of(negotiation));

        NegotiationResponseDto response =
                negotiationService
                        .getNegotiationById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(
                "NEG-1",
                response.getNegotiationNumber());
        assertEquals(
                1L,
                response.getPurchaseRequestId());
        assertEquals(
                NegotiationStatus.ACTIVE,
                response.getStatus());

        verify(negotiationRepository, times(1))
                .findById(1L);
    }

    @Test
    void getNegotiationById_shouldThrowExceptionWhenNotFound() {

        when(negotiationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> negotiationService
                        .getNegotiationById(999L));

        verify(negotiationRepository, times(1))
                .findById(999L);
    }

    @Test
    void getOffers_shouldReturnOffers() {

        Negotiation negotiation =
                createNegotiation(
                        1L,
                        NegotiationStatus.ACTIVE);

        NegotiationOffer offer1 =
                createOffer(
                        1L,
                        1L,
                        401L,
                        "DEALER",
                        new BigDecimal("50.00"),
                        OfferStatus.PENDING);

        NegotiationOffer offer2 =
                createOffer(
                        2L,
                        1L,
                        201L,
                        "FARMER",
                        new BigDecimal("55.00"),
                        OfferStatus.PENDING);

        when(negotiationRepository.findById(1L))
                .thenReturn(Optional.of(negotiation));

        when(negotiationOfferRepository
                .findByNegotiationIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(offer1, offer2));

        List<OfferResponseDto> response =
                negotiationService.getOffers(1L);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals(2L, response.get(1).getId());

        verify(negotiationRepository, times(1))
                .findById(1L);

        verify(negotiationOfferRepository, times(1))
                .findByNegotiationIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void createOffer_shouldCreateSuccessfully() {

        Negotiation negotiation =
                createNegotiation(
                        1L,
                        NegotiationStatus.ACTIVE);

        CreateOfferRequestDto requestDto =
                new CreateOfferRequestDto();

        requestDto.setPrice(
                new BigDecimal("55.00"));
        requestDto.setQuantity(
                new BigDecimal("100"));
        requestDto.setMessage(
                "My final offer");

        when(negotiationRepository.findById(1L))
                .thenReturn(Optional.of(negotiation));

        when(negotiationOfferRepository
                .save(any(NegotiationOffer.class)))
                .thenAnswer(invocation -> {
                    NegotiationOffer offer =
                            invocation.getArgument(0);

                    offer.setId(10L);

                    return offer;
                });

        OfferResponseDto response =
                negotiationService.createOffer(
                        1L,
                        requestDto,
                        401L,
                        "DEALER");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getNegotiationId());
        assertEquals(401L, response.getOfferedByUserId());
        assertEquals("DEALER", response.getOfferedByRole());
        assertEquals(
                new BigDecimal("55.00"),
                response.getPrice());
        assertEquals(
                new BigDecimal("100"),
                response.getQuantity());
        assertEquals(
                OfferStatus.PENDING,
                response.getOfferStatus());

        verify(negotiationOfferRepository, times(1))
                .save(any(NegotiationOffer.class));
    }

    @Test
    void createOffer_shouldThrowExceptionWhenNegotiationNotActive() {

        Negotiation negotiation =
                createNegotiation(
                        1L,
                        NegotiationStatus.ACCEPTED);

        CreateOfferRequestDto requestDto =
                new CreateOfferRequestDto();

        requestDto.setPrice(
                new BigDecimal("55.00"));
        requestDto.setQuantity(
                new BigDecimal("100"));

        when(negotiationRepository.findById(1L))
                .thenReturn(Optional.of(negotiation));

        assertThrows(
                RuntimeException.class,
                () -> negotiationService.createOffer(
                        1L,
                        requestDto,
                        401L,
                        "DEALER"));

        verify(negotiationOfferRepository, never())
                .save(any(NegotiationOffer.class));
    }

    @Test
    void createCounterOffer_shouldCreateSuccessfully() {

        Negotiation negotiation =
                createNegotiation(
                        1L,
                        NegotiationStatus.ACTIVE);

        NegotiationOffer previousOffer =
                createOffer(
                        1L,
                        1L,
                        401L,
                        "DEALER",
                        new BigDecimal("50.00"),
                        OfferStatus.PENDING);

        CounterOfferRequestDto requestDto =
                new CounterOfferRequestDto();

        requestDto.setPrice(
                new BigDecimal("60.00"));
        requestDto.setQuantity(
                new BigDecimal("100"));
        requestDto.setMessage(
                "Counter offer");

        when(negotiationRepository.findById(1L))
                .thenReturn(Optional.of(negotiation));

        when(negotiationOfferRepository
                .findByNegotiationIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(previousOffer));

        when(negotiationOfferRepository
                .save(any(NegotiationOffer.class)))
                .thenAnswer(invocation -> {
                    NegotiationOffer offer =
                            invocation.getArgument(0);

                    if (offer.getId() == null) {
                        offer.setId(2L);
                    }

                    return offer;
                });

        OfferResponseDto response =
                negotiationService.createCounterOffer(
                        1L,
                        requestDto,
                        201L,
                        "FARMER");

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals(1L, response.getNegotiationId());
        assertEquals(201L, response.getOfferedByUserId());
        assertEquals("FARMER", response.getOfferedByRole());
        assertEquals(
                new BigDecimal("60.00"),
                response.getPrice());
        assertEquals(
                OfferStatus.PENDING,
                response.getOfferStatus());

        assertEquals(
                OfferStatus.COUNTERED,
                previousOffer.getOfferStatus());

        verify(
                negotiationOfferRepository,
                atLeastOnce())
                .save(any(NegotiationOffer.class));
    }

    @Test
    void acceptOffer_shouldAcceptOfferAndNegotiation() {

        Negotiation negotiation =
                createNegotiation(
                        1L,
                        NegotiationStatus.ACTIVE);

        NegotiationOffer acceptedOffer =
                createOffer(
                        1L,
                        1L,
                        401L,
                        "DEALER",
                        new BigDecimal("55.00"),
                        OfferStatus.PENDING);

        NegotiationOffer otherOffer =
                createOffer(
                        2L,
                        1L,
                        201L,
                        "FARMER",
                        new BigDecimal("60.00"),
                        OfferStatus.PENDING);

        when(negotiationRepository.findById(1L))
                .thenReturn(Optional.of(negotiation));

        when(negotiationOfferRepository.findById(1L))
                .thenReturn(Optional.of(acceptedOffer));

        when(negotiationOfferRepository
                .findByNegotiationIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(acceptedOffer, otherOffer));

        when(negotiationOfferRepository
                .save(any(NegotiationOffer.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(negotiationRepository
                .save(any(Negotiation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        NegotiationResponseDto response =
                negotiationService.acceptOffer(
                        1L,
                        1L);

        assertNotNull(response);
        assertEquals(
                NegotiationStatus.ACCEPTED,
                response.getStatus());
        assertEquals(
                new BigDecimal("55.00"),
                response.getFinalPrice());

        assertEquals(
                OfferStatus.ACCEPTED,
                acceptedOffer.getOfferStatus());

        assertEquals(
                OfferStatus.REJECTED,
                otherOffer.getOfferStatus());

        verify(negotiationRepository, times(1))
                .save(negotiation);
    }

    @Test
    void acceptOffer_shouldThrowExceptionWhenOfferBelongsToAnotherNegotiation() {

        Negotiation negotiation =
                createNegotiation(
                        1L,
                        NegotiationStatus.ACTIVE);

        NegotiationOffer offer =
                createOffer(
                        10L,
                        99L,
                        401L,
                        "DEALER",
                        new BigDecimal("55.00"),
                        OfferStatus.PENDING);

        when(negotiationRepository.findById(1L))
                .thenReturn(Optional.of(negotiation));

        when(negotiationOfferRepository.findById(10L))
                .thenReturn(Optional.of(offer));

        assertThrows(
                RuntimeException.class,
                () -> negotiationService
                        .acceptOffer(1L, 10L));

        verify(negotiationRepository, never())
                .save(any(Negotiation.class));
    }

    @Test
    void rejectOffer_shouldRejectOnlySelectedOffer() {

        Negotiation negotiation =
                createNegotiation(
                        1L,
                        NegotiationStatus.ACTIVE);

        NegotiationOffer offer =
                createOffer(
                        1L,
                        1L,
                        401L,
                        "DEALER",
                        new BigDecimal("55.00"),
                        OfferStatus.PENDING);

        when(negotiationRepository.findById(1L))
                .thenReturn(Optional.of(negotiation));

        when(negotiationOfferRepository.findById(1L))
                .thenReturn(Optional.of(offer));

        when(negotiationOfferRepository
                .save(any(NegotiationOffer.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(negotiationRepository
                .save(any(Negotiation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        NegotiationResponseDto response =
                negotiationService.rejectOffer(
                        1L,
                        1L);

        assertNotNull(response);

        assertEquals(
                NegotiationStatus.ACTIVE,
                response.getStatus());

        assertEquals(
                OfferStatus.REJECTED,
                offer.getOfferStatus());

        verify(negotiationOfferRepository, times(1))
                .save(offer);

        verify(negotiationRepository, times(1))
                .save(negotiation);
    }

    private PurchaseRequest createPurchaseRequest() {

        PurchaseRequest request =
                new PurchaseRequest();

        request.setId(1L);
        request.setRequestNumber("REQ-1");
        request.setCropId(101L);
        request.setFarmerId(201L);
        request.setDealerId(301L);
        request.setRequestedQuantity(
                new BigDecimal("100"));
        request.setOfferedPrice(
                new BigDecimal("50.00"));
        request.setMessage("Test request");
        request.setStatus(
                PurchaseRequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        return request;
    }

    private Negotiation createNegotiation(
            Long id,
            NegotiationStatus status) {

        Negotiation negotiation =
                new Negotiation();

        negotiation.setId(id);
        negotiation.setNegotiationNumber(
                "NEG-" + id);
        negotiation.setPurchaseRequestId(1L);
        negotiation.setCropId(101L);
        negotiation.setFarmerId(201L);
        negotiation.setDealerId(301L);
        negotiation.setStatus(status);
        negotiation.setFinalPrice(null);
        negotiation.setCreatedAt(LocalDateTime.now());
        negotiation.setUpdatedAt(LocalDateTime.now());

        return negotiation;
    }

    private NegotiationOffer createOffer(
            Long id,
            Long negotiationId,
            Long userId,
            String role,
            BigDecimal price,
            OfferStatus status) {

        NegotiationOffer offer =
                new NegotiationOffer();

        offer.setId(id);
        offer.setNegotiationId(negotiationId);
        offer.setOfferedByUserId(userId);
        offer.setOfferedByRole(role);
        offer.setPrice(price);
        offer.setQuantity(
                new BigDecimal("100"));
        offer.setMessage("Test offer");
        offer.setOfferStatus(status);
        offer.setCreatedAt(LocalDateTime.now());

        return offer;
    }
}