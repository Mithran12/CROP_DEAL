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

import com.cropdeal.order.dto.request.CreatePurchaseRequestDto;
import com.cropdeal.order.dto.response.PurchaseRequestResponseDto;
import com.cropdeal.order.entity.PurchaseRequest;
import com.cropdeal.order.enums.PurchaseRequestStatus;
import com.cropdeal.order.repository.PurchaseRequestRepository;
import com.cropdeal.order.client.ExternalServiceClientService;

@ExtendWith(MockitoExtension.class)
class PurchaseRequestServiceImplTest {

	@Mock
	private PurchaseRequestRepository purchaseRequestRepository;

	@Mock
	private ExternalServiceClientService externalServiceClientService;
	
	@InjectMocks
	private PurchaseRequestServiceImpl purchaseRequestService;

	@Test
	void createPurchaseRequest_shouldCreateSuccessfully() {

		CreatePurchaseRequestDto requestDto = new CreatePurchaseRequestDto();

		requestDto.setCropId(101L);
		requestDto.setFarmerId(201L);
		requestDto.setRequestedQuantity(new BigDecimal("100"));
		requestDto.setOfferedPrice(new BigDecimal("50.00"));
		requestDto.setMessage("Need good quality crop");

		Long dealerId = 301L;

		when(externalServiceClientService.getCropById(anyLong())).thenReturn(new Object());

		when(externalServiceClientService.getUserById(anyLong())).thenReturn(new Object());

		when(purchaseRequestRepository.save(any(PurchaseRequest.class))).thenAnswer(invocation -> {
			PurchaseRequest request = invocation.getArgument(0);

			request.setId(1L);

			return request;
		});

		PurchaseRequestResponseDto response = purchaseRequestService.createPurchaseRequest(requestDto, dealerId);

		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals(101L, response.getCropId());
		assertEquals(201L, response.getFarmerId());
		assertEquals(301L, response.getDealerId());
		assertEquals(new BigDecimal("100"), response.getRequestedQuantity());
		assertEquals(new BigDecimal("50.00"), response.getOfferedPrice());
		assertEquals(PurchaseRequestStatus.PENDING, response.getStatus());

		verify(purchaseRequestRepository, times(1)).save(any(PurchaseRequest.class));
	}

	@Test
	void getPurchaseRequestById_shouldReturnSuccessfully() {

		PurchaseRequest request = new PurchaseRequest();

		request.setId(1L);
		request.setRequestNumber("REQ-1001");
		request.setCropId(101L);
		request.setFarmerId(201L);
		request.setDealerId(301L);
		request.setRequestedQuantity(new BigDecimal("100"));
		request.setOfferedPrice(new BigDecimal("50.00"));
		request.setMessage("Test request");
		request.setStatus(PurchaseRequestStatus.PENDING);
		request.setCreatedAt(LocalDateTime.now());
		request.setUpdatedAt(LocalDateTime.now());

		when(purchaseRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		PurchaseRequestResponseDto response = purchaseRequestService.getPurchaseRequestById(1L);

		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals("REQ-1001", response.getRequestNumber());
		assertEquals(101L, response.getCropId());
		assertEquals(201L, response.getFarmerId());
		assertEquals(301L, response.getDealerId());
		assertEquals(PurchaseRequestStatus.PENDING, response.getStatus());

		verify(purchaseRequestRepository, times(1)).findById(1L);
	}

	@Test
	void getPurchaseRequestById_shouldThrowExceptionWhenNotFound() {

		when(purchaseRequestRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> purchaseRequestService.getPurchaseRequestById(999L));

		verify(purchaseRequestRepository, times(1)).findById(999L);
	}

	@Test
	void getPurchaseRequestsByFarmer_shouldReturnList() {

		PurchaseRequest request1 = createPurchaseRequest(1L, 201L, 301L, PurchaseRequestStatus.PENDING);

		PurchaseRequest request2 = createPurchaseRequest(2L, 201L, 302L, PurchaseRequestStatus.ACCEPTED);

		when(purchaseRequestRepository.findByFarmerId(201L)).thenReturn(List.of(request1, request2));

		List<PurchaseRequestResponseDto> response = purchaseRequestService.getPurchaseRequestsByFarmer(201L);

		assertNotNull(response);
		assertEquals(2, response.size());
		assertEquals(1L, response.get(0).getId());
		assertEquals(2L, response.get(1).getId());

		verify(purchaseRequestRepository, times(1)).findByFarmerId(201L);
	}

	@Test
	void getPurchaseRequestsByDealer_shouldReturnList() {

		PurchaseRequest request1 = createPurchaseRequest(1L, 201L, 301L, PurchaseRequestStatus.PENDING);

		PurchaseRequest request2 = createPurchaseRequest(2L, 202L, 301L, PurchaseRequestStatus.REJECTED);

		when(purchaseRequestRepository.findByDealerId(301L)).thenReturn(List.of(request1, request2));

		List<PurchaseRequestResponseDto> response = purchaseRequestService.getPurchaseRequestsByDealer(301L);

		assertNotNull(response);
		assertEquals(2, response.size());
		assertEquals(1L, response.get(0).getId());
		assertEquals(2L, response.get(1).getId());

		verify(purchaseRequestRepository, times(1)).findByDealerId(301L);
	}

	@Test
	void acceptPurchaseRequest_shouldChangeStatusToAccepted() {

		PurchaseRequest request = createPurchaseRequest(1L, 201L, 301L, PurchaseRequestStatus.PENDING);

		when(purchaseRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		when(purchaseRequestRepository.save(any(PurchaseRequest.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		PurchaseRequestResponseDto response = purchaseRequestService.acceptPurchaseRequest(1L);

		assertNotNull(response);
		assertEquals(PurchaseRequestStatus.ACCEPTED, response.getStatus());

		verify(purchaseRequestRepository, times(1)).save(request);
	}

	@Test
	void acceptPurchaseRequest_shouldThrowExceptionWhenNotPending() {

		PurchaseRequest request = createPurchaseRequest(1L, 201L, 301L, PurchaseRequestStatus.REJECTED);

		when(purchaseRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		assertThrows(RuntimeException.class, () -> purchaseRequestService.acceptPurchaseRequest(1L));

		verify(purchaseRequestRepository, never()).save(any(PurchaseRequest.class));
	}

	@Test
	void rejectPurchaseRequest_shouldChangeStatusToRejected() {

		PurchaseRequest request = createPurchaseRequest(1L, 201L, 301L, PurchaseRequestStatus.PENDING);

		when(purchaseRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		when(purchaseRequestRepository.save(any(PurchaseRequest.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		PurchaseRequestResponseDto response = purchaseRequestService.rejectPurchaseRequest(1L);

		assertNotNull(response);
		assertEquals(PurchaseRequestStatus.REJECTED, response.getStatus());

		verify(purchaseRequestRepository, times(1)).save(request);
	}

	@Test
	void rejectPurchaseRequest_shouldThrowExceptionWhenNotPending() {

		PurchaseRequest request = createPurchaseRequest(1L, 201L, 301L, PurchaseRequestStatus.ACCEPTED);

		when(purchaseRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		assertThrows(RuntimeException.class, () -> purchaseRequestService.rejectPurchaseRequest(1L));

		verify(purchaseRequestRepository, never()).save(any(PurchaseRequest.class));
	}

	@Test
	void cancelPurchaseRequest_shouldChangeStatusToCancelled() {

		PurchaseRequest request = createPurchaseRequest(1L, 201L, 301L, PurchaseRequestStatus.PENDING);

		when(purchaseRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		when(purchaseRequestRepository.save(any(PurchaseRequest.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		PurchaseRequestResponseDto response = purchaseRequestService.cancelPurchaseRequest(1L);

		assertNotNull(response);
		assertEquals(PurchaseRequestStatus.CANCELLED, response.getStatus());

		verify(purchaseRequestRepository, times(1)).save(request);
	}

	@Test
	void cancelPurchaseRequest_shouldThrowExceptionWhenNotPending() {

		PurchaseRequest request = createPurchaseRequest(1L, 201L, 301L, PurchaseRequestStatus.ACCEPTED);

		when(purchaseRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		assertThrows(RuntimeException.class, () -> purchaseRequestService.cancelPurchaseRequest(1L));

		verify(purchaseRequestRepository, never()).save(any(PurchaseRequest.class));
	}

	private PurchaseRequest createPurchaseRequest(Long id, Long farmerId, Long dealerId, PurchaseRequestStatus status) {

		PurchaseRequest request = new PurchaseRequest();

		request.setId(id);
		request.setRequestNumber("REQ-" + id);
		request.setCropId(101L);
		request.setFarmerId(farmerId);
		request.setDealerId(dealerId);
		request.setRequestedQuantity(new BigDecimal("100"));
		request.setOfferedPrice(new BigDecimal("50.00"));
		request.setMessage("Test request");
		request.setStatus(status);
		request.setCreatedAt(LocalDateTime.now());
		request.setUpdatedAt(LocalDateTime.now());

		return request;
	}
}