package com.cropdeal.order.service;

import java.time.LocalDateTime;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cropdeal.order.client.ExternalServiceClientService;
import com.cropdeal.order.dto.request.CreatePurchaseRequestDto;
import com.cropdeal.order.dto.response.PurchaseRequestResponseDto;
import com.cropdeal.order.entity.PurchaseRequest;
import com.cropdeal.order.enums.PurchaseRequestStatus;
import com.cropdeal.order.exception.BusinessException;
import com.cropdeal.order.exception.ResourceNotFoundException;
import com.cropdeal.order.repository.PurchaseRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseRequestServiceImpl implements PurchaseRequestService {

	private final PurchaseRequestRepository purchaseRequestRepository;
	private final ExternalServiceClientService externalServiceClientService;

	// 1. CREATE PURCHASE REQUEST

	@Override
	@Transactional
	public PurchaseRequestResponseDto createPurchaseRequest(CreatePurchaseRequestDto requestDto, Long dealerId) {

		// Verify crop exists in Crop Service
		externalServiceClientService.getCropById(
		        requestDto.getCropId());

		// Verify farmer exists in User Service
		externalServiceClientService.getUserById(
		        requestDto.getFarmerId());

		PurchaseRequest purchaseRequest = new PurchaseRequest();
		purchaseRequest.setCropId(requestDto.getCropId());
		purchaseRequest.setFarmerId(requestDto.getFarmerId());

		// Dealer ID comes from authenticated user 
		purchaseRequest.setDealerId(dealerId);

		purchaseRequest.setRequestedQuantity(requestDto.getRequestedQuantity());

		purchaseRequest.setOfferedPrice(requestDto.getOfferedPrice());

		purchaseRequest.setMessage(requestDto.getMessage());

		purchaseRequest.setRequestNumber("REQ-" + System.currentTimeMillis());

		purchaseRequest.setStatus(PurchaseRequestStatus.PENDING);

		LocalDateTime now = LocalDateTime.now();

		purchaseRequest.setCreatedAt(now);
		purchaseRequest.setUpdatedAt(now);

		PurchaseRequest savedRequest = purchaseRequestRepository.save(purchaseRequest);

		return mapToResponseDto(savedRequest);
	}

	// 2. GET PURCHASE REQUEST BY ID

	@Override
	public PurchaseRequestResponseDto getPurchaseRequestById(Long id) {

		PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

		return mapToResponseDto(purchaseRequest);
	}

	// 3. GET PURCHASE REQUESTS BY FARMER

	@Override
	public List<PurchaseRequestResponseDto> getPurchaseRequestsByFarmer(Long farmerId) {

		return purchaseRequestRepository.findByFarmerId(farmerId).stream().map(this::mapToResponseDto).toList();
	}

	// 4. GET PURCHASE REQUESTS BY DEALER

	@Override
	public List<PurchaseRequestResponseDto> getPurchaseRequestsByDealer(Long dealerId) {

		return purchaseRequestRepository.findByDealerId(dealerId).stream().map(this::mapToResponseDto).toList();
	}

	// 5. ACCEPT PURCHASE REQUEST

	@Override
	@Transactional
	public PurchaseRequestResponseDto acceptPurchaseRequest(Long id) {

		PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

		if (purchaseRequest.getStatus() != PurchaseRequestStatus.PENDING) {
			throw new BusinessException("Only pending purchase requests can be accepted");
		}

		purchaseRequest.setStatus(PurchaseRequestStatus.ACCEPTED);
		purchaseRequest.setUpdatedAt(LocalDateTime.now());

		PurchaseRequest updatedRequest = purchaseRequestRepository.save(purchaseRequest);

		return mapToResponseDto(updatedRequest);
	}

	// 6. REJECT PURCHASE REQUEST

	@Override
	@Transactional
	public PurchaseRequestResponseDto rejectPurchaseRequest(Long id) {

		PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

		if (purchaseRequest.getStatus() != PurchaseRequestStatus.PENDING) {
			throw new BusinessException("Only pending purchase requests can be rejected");
		}

		purchaseRequest.setStatus(PurchaseRequestStatus.REJECTED);
		purchaseRequest.setUpdatedAt(LocalDateTime.now());

		PurchaseRequest updatedRequest = purchaseRequestRepository.save(purchaseRequest);

		return mapToResponseDto(updatedRequest);
	}

	// 7. CANCEL PURCHASE REQUEST

	@Override
	@Transactional
	public PurchaseRequestResponseDto cancelPurchaseRequest(Long id) {

		PurchaseRequest purchaseRequest = purchaseRequestRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + id));

		if (purchaseRequest.getStatus() != PurchaseRequestStatus.PENDING) {
			throw new BusinessException("Only pending purchase requests can be cancelled");
		}

		purchaseRequest.setStatus(PurchaseRequestStatus.CANCELLED);
		purchaseRequest.setUpdatedAt(LocalDateTime.now());

		PurchaseRequest updatedRequest = purchaseRequestRepository.save(purchaseRequest);

		return mapToResponseDto(updatedRequest);
	}

	// ENTITY → RESPONSE DTO

	private PurchaseRequestResponseDto mapToResponseDto(PurchaseRequest purchaseRequest) {

		return PurchaseRequestResponseDto.builder().id(purchaseRequest.getId())
				.requestNumber(purchaseRequest.getRequestNumber()).cropId(purchaseRequest.getCropId())
				.farmerId(purchaseRequest.getFarmerId()).dealerId(purchaseRequest.getDealerId())
				.requestedQuantity(purchaseRequest.getRequestedQuantity())
				.offeredPrice(purchaseRequest.getOfferedPrice()).message(purchaseRequest.getMessage())
				.status(purchaseRequest.getStatus()).createdAt(purchaseRequest.getCreatedAt())
				.updatedAt(purchaseRequest.getUpdatedAt()).build();
	}

}