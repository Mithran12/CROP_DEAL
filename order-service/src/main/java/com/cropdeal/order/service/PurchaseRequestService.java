package com.cropdeal.order.service;

import java.util.List;

import com.cropdeal.order.dto.request.CreatePurchaseRequestDto;
import com.cropdeal.order.dto.response.PurchaseRequestResponseDto;

public interface PurchaseRequestService {

    PurchaseRequestResponseDto createPurchaseRequest(
            CreatePurchaseRequestDto requestDto,
            Long dealerId);

    PurchaseRequestResponseDto getPurchaseRequestById(Long id);

    List<PurchaseRequestResponseDto> getPurchaseRequestsByFarmer(Long farmerId);

    List<PurchaseRequestResponseDto> getPurchaseRequestsByDealer(Long dealerId);

    PurchaseRequestResponseDto acceptPurchaseRequest(Long id);

    PurchaseRequestResponseDto rejectPurchaseRequest(Long id);

    PurchaseRequestResponseDto cancelPurchaseRequest(Long id);
}