package com.cropdeal.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cropdeal.order.client.ExternalServiceClientService;
import com.cropdeal.order.dto.request.CreatePurchaseRequestDto;
import com.cropdeal.order.dto.response.PurchaseRequestResponseDto;
import com.cropdeal.order.entity.PurchaseRequest;
import com.cropdeal.order.enums.PurchaseRequestStatus;
import com.cropdeal.order.exception.BusinessException;
import com.cropdeal.order.exception.ResourceNotFoundException;
import com.cropdeal.order.exception.UnauthorizedResourceException;
import com.cropdeal.order.repository.PurchaseRequestRepository;

@ExtendWith(MockitoExtension.class)
class PurchaseRequestServiceImplTest {

    @Mock
    private PurchaseRequestRepository purchaseRequestRepository;

    @Mock
    private ExternalServiceClientService externalServiceClientService;

    @InjectMocks
    private PurchaseRequestServiceImpl purchaseRequestService;

    // =========================================================
    // 1. CREATE PURCHASE REQUEST
    // =========================================================

    @Test
    void createPurchaseRequest_shouldCreateSuccessfully() {

        CreatePurchaseRequestDto requestDto =
                new CreatePurchaseRequestDto();

        requestDto.setCropId(101L);
        requestDto.setRequestedQuantity(
                new BigDecimal("100"));
        requestDto.setOfferedPrice(
                new BigDecimal("50.00"));
        requestDto.setMessage(
                "Need good quality crop");

        Long dealerId = 301L;

        when(externalServiceClientService.getCropById(anyLong()))
                .thenReturn(Map.of("farmerId", 201L));

        when(purchaseRequestRepository.save(
                any(PurchaseRequest.class)))
                .thenAnswer(invocation -> {

                    PurchaseRequest request =
                            invocation.getArgument(0);

                    request.setId(1L);

                    return request;
                });

        PurchaseRequestResponseDto response =
                purchaseRequestService.createPurchaseRequest(
                        requestDto,
                        dealerId);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(101L, response.getCropId());
        assertEquals(201L, response.getFarmerId());
        assertEquals(301L, response.getDealerId());
        assertEquals(
                new BigDecimal("100"),
                response.getRequestedQuantity());
        assertEquals(
                new BigDecimal("50.00"),
                response.getOfferedPrice());
        assertEquals(
                PurchaseRequestStatus.PENDING,
                response.getStatus());

        verify(purchaseRequestRepository, times(1))
                .save(any(PurchaseRequest.class));
    }

    // =========================================================
    // 2. GET PURCHASE REQUEST BY ID
    // =========================================================

    @Test
    void getPurchaseRequestById_shouldReturnSuccessfully() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.PENDING);

        request.setRequestNumber("REQ-1001");

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        // Dealer 301 is associated with the request
        PurchaseRequestResponseDto response =
                purchaseRequestService.getPurchaseRequestById(
                        1L,
                        301L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(
                "REQ-1001",
                response.getRequestNumber());
        assertEquals(
                101L,
                response.getCropId());
        assertEquals(
                201L,
                response.getFarmerId());
        assertEquals(
                301L,
                response.getDealerId());
        assertEquals(
                PurchaseRequestStatus.PENDING,
                response.getStatus());

        verify(purchaseRequestRepository, times(1))
                .findById(1L);
    }

    @Test
    void getPurchaseRequestById_shouldReturnSuccessfullyForFarmer() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.PENDING);

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        // Farmer 201 is associated with the request
        PurchaseRequestResponseDto response =
                purchaseRequestService.getPurchaseRequestById(
                        1L,
                        201L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(201L, response.getFarmerId());
        assertEquals(301L, response.getDealerId());
    }

    @Test
    void getPurchaseRequestById_shouldThrowExceptionWhenNotFound() {

        when(purchaseRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> purchaseRequestService
                        .getPurchaseRequestById(
                                999L,
                                301L));

        verify(purchaseRequestRepository, times(1))
                .findById(999L);
    }

    @Test
    void getPurchaseRequestById_shouldThrowUnauthorizedExceptionForUnrelatedUser() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.PENDING);

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        // User 401 is neither the farmer nor the dealer
        assertThrows(
                UnauthorizedResourceException.class,
                () -> purchaseRequestService
                        .getPurchaseRequestById(
                                1L,
                                401L));

        verify(purchaseRequestRepository, times(1))
                .findById(1L);
    }

    // =========================================================
    // 3. GET PURCHASE REQUESTS BY FARMER
    // =========================================================

    @Test
    void getPurchaseRequestsByFarmer_shouldReturnList() {

        PurchaseRequest request1 =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.PENDING);

        PurchaseRequest request2 =
                createPurchaseRequest(
                        2L,
                        201L,
                        302L,
                        PurchaseRequestStatus.ACCEPTED);

        when(purchaseRequestRepository.findByFarmerId(201L))
                .thenReturn(List.of(request1, request2));

        List<PurchaseRequestResponseDto> response =
                purchaseRequestService
                        .getPurchaseRequestsByFarmer(
                                201L,
                                201L);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals(2L, response.get(1).getId());

        verify(purchaseRequestRepository, times(1))
                .findByFarmerId(201L);
    }

    @Test
    void getPurchaseRequestsByFarmer_shouldThrowUnauthorizedExceptionForAnotherFarmer() {

        assertThrows(
                UnauthorizedResourceException.class,
                () -> purchaseRequestService
                        .getPurchaseRequestsByFarmer(
                                201L,
                                202L));

        verify(
                purchaseRequestRepository,
                never())
                .findByFarmerId(anyLong());
    }

    // =========================================================
    // 4. GET PURCHASE REQUESTS BY DEALER
    // =========================================================

    @Test
    void getPurchaseRequestsByDealer_shouldReturnList() {

        PurchaseRequest request1 =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.PENDING);

        PurchaseRequest request2 =
                createPurchaseRequest(
                        2L,
                        202L,
                        301L,
                        PurchaseRequestStatus.REJECTED);

        when(purchaseRequestRepository.findByDealerId(301L))
                .thenReturn(List.of(request1, request2));

        List<PurchaseRequestResponseDto> response =
                purchaseRequestService
                        .getPurchaseRequestsByDealer(
                                301L,
                                301L);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals(2L, response.get(1).getId());

        verify(purchaseRequestRepository, times(1))
                .findByDealerId(301L);
    }

    @Test
    void getPurchaseRequestsByDealer_shouldThrowUnauthorizedExceptionForAnotherDealer() {

        assertThrows(
                UnauthorizedResourceException.class,
                () -> purchaseRequestService
                        .getPurchaseRequestsByDealer(
                                301L,
                                302L));

        verify(
                purchaseRequestRepository,
                never())
                .findByDealerId(anyLong());
    }

    // =========================================================
    // 5. ACCEPT PURCHASE REQUEST
    // =========================================================

    @Test
    void acceptPurchaseRequest_shouldChangeStatusToAccepted() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.PENDING);

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(purchaseRequestRepository.save(
                any(PurchaseRequest.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        // Farmer 201 accepts the request
        PurchaseRequestResponseDto response =
                purchaseRequestService.acceptPurchaseRequest(
                        1L,
                        201L);

        assertNotNull(response);
        assertEquals(
                PurchaseRequestStatus.ACCEPTED,
                response.getStatus());

        verify(
                purchaseRequestRepository,
                times(1))
                .save(request);
    }

    @Test
    void acceptPurchaseRequest_shouldThrowUnauthorizedExceptionForAnotherFarmer() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.PENDING);

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        // Farmer 202 tries to accept Farmer 201's request
        assertThrows(
                UnauthorizedResourceException.class,
                () -> purchaseRequestService
                        .acceptPurchaseRequest(
                                1L,
                                202L));

        verify(
                purchaseRequestRepository,
                never())
                .save(any(PurchaseRequest.class));
    }

    @Test
    void acceptPurchaseRequest_shouldThrowExceptionWhenNotPending() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.REJECTED);

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        assertThrows(
                BusinessException.class,
                () -> purchaseRequestService
                        .acceptPurchaseRequest(
                                1L,
                                201L));

        verify(
                purchaseRequestRepository,
                never())
                .save(any(PurchaseRequest.class));
    }

    @Test
    void acceptPurchaseRequest_shouldThrowExceptionWhenNotFound() {

        when(purchaseRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> purchaseRequestService
                        .acceptPurchaseRequest(
                                999L,
                                201L));

        verify(
                purchaseRequestRepository,
                never())
                .save(any(PurchaseRequest.class));
    }

    // =========================================================
    // 6. REJECT PURCHASE REQUEST
    // =========================================================

    @Test
    void rejectPurchaseRequest_shouldChangeStatusToRejected() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.PENDING);

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(purchaseRequestRepository.save(
                any(PurchaseRequest.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        // Farmer 201 rejects the request
        PurchaseRequestResponseDto response =
                purchaseRequestService.rejectPurchaseRequest(
                        1L,
                        201L);

        assertNotNull(response);
        assertEquals(
                PurchaseRequestStatus.REJECTED,
                response.getStatus());

        verify(
                purchaseRequestRepository,
                times(1))
                .save(request);
    }

    @Test
    void rejectPurchaseRequest_shouldThrowUnauthorizedExceptionForAnotherFarmer() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.PENDING);

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        // Farmer 202 tries to reject Farmer 201's request
        assertThrows(
                UnauthorizedResourceException.class,
                () -> purchaseRequestService
                        .rejectPurchaseRequest(
                                1L,
                                202L));

        verify(
                purchaseRequestRepository,
                never())
                .save(any(PurchaseRequest.class));
    }

    @Test
    void rejectPurchaseRequest_shouldThrowExceptionWhenNotPending() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.ACCEPTED);

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        assertThrows(
                BusinessException.class,
                () -> purchaseRequestService
                        .rejectPurchaseRequest(
                                1L,
                                201L));

        verify(
                purchaseRequestRepository,
                never())
                .save(any(PurchaseRequest.class));
    }

    @Test
    void rejectPurchaseRequest_shouldThrowExceptionWhenNotFound() {

        when(purchaseRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> purchaseRequestService
                        .rejectPurchaseRequest(
                                999L,
                                201L));

        verify(
                purchaseRequestRepository,
                never())
                .save(any(PurchaseRequest.class));
    }

    // =========================================================
    // 7. CANCEL PURCHASE REQUEST
    // =========================================================

    @Test
    void cancelPurchaseRequest_shouldChangeStatusToCancelled() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.PENDING);

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(purchaseRequestRepository.save(
                any(PurchaseRequest.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        // Dealer 301 cancels their own request
        PurchaseRequestResponseDto response =
                purchaseRequestService.cancelPurchaseRequest(
                        1L,
                        301L);

        assertNotNull(response);
        assertEquals(
                PurchaseRequestStatus.CANCELLED,
                response.getStatus());

        verify(
                purchaseRequestRepository,
                times(1))
                .save(request);
    }

    @Test
    void cancelPurchaseRequest_shouldThrowUnauthorizedExceptionForAnotherDealer() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.PENDING);

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        // Dealer 302 tries to cancel Dealer 301's request
        assertThrows(
                UnauthorizedResourceException.class,
                () -> purchaseRequestService
                        .cancelPurchaseRequest(
                                1L,
                                302L));

        verify(
                purchaseRequestRepository,
                never())
                .save(any(PurchaseRequest.class));
    }

    @Test
    void cancelPurchaseRequest_shouldThrowExceptionWhenNotPending() {

        PurchaseRequest request =
                createPurchaseRequest(
                        1L,
                        201L,
                        301L,
                        PurchaseRequestStatus.ACCEPTED);

        when(purchaseRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        assertThrows(
                BusinessException.class,
                () -> purchaseRequestService
                        .cancelPurchaseRequest(
                                1L,
                                301L));

        verify(
                purchaseRequestRepository,
                never())
                .save(any(PurchaseRequest.class));
    }

    @Test
    void cancelPurchaseRequest_shouldThrowExceptionWhenNotFound() {

        when(purchaseRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> purchaseRequestService
                        .cancelPurchaseRequest(
                                999L,
                                301L));

        verify(
                purchaseRequestRepository,
                never())
                .save(any(PurchaseRequest.class));
    }

    // =========================================================
    // HELPER METHOD
    // =========================================================

    private PurchaseRequest createPurchaseRequest(
            Long id,
            Long farmerId,
            Long dealerId,
            PurchaseRequestStatus status) {

        PurchaseRequest request =
                new PurchaseRequest();

        request.setId(id);
        request.setRequestNumber(
                "REQ-" + id);
        request.setCropId(101L);
        request.setFarmerId(farmerId);
        request.setDealerId(dealerId);
        request.setRequestedQuantity(
                new BigDecimal("100"));
        request.setOfferedPrice(
                new BigDecimal("50.00"));
        request.setMessage(
                "Test request");
        request.setStatus(status);
        request.setCreatedAt(
                LocalDateTime.now());
        request.setUpdatedAt(
                LocalDateTime.now());

        return request;
    }
}