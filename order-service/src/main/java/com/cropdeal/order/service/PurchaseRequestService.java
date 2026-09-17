package com.cropdeal.order.service;

import java.util.List;

import com.cropdeal.order.dto.request.CreatePurchaseRequestDto;
import com.cropdeal.order.dto.response.PurchaseRequestResponseDto;

public interface PurchaseRequestService {

    PurchaseRequestResponseDto createPurchaseRequest(
            CreatePurchaseRequestDto requestDto,
            Long dealerId);

    PurchaseRequestResponseDto getPurchaseRequestById(
            Long id,
            Long authenticatedUserId);

    List<PurchaseRequestResponseDto> getPurchaseRequestsByFarmer(
            Long farmerId,
            Long authenticatedUserId);

    List<PurchaseRequestResponseDto> getPurchaseRequestsByDealer(
            Long dealerId,
            Long authenticatedUserId);

    PurchaseRequestResponseDto acceptPurchaseRequest(
            Long id,
            Long authenticatedUserId);

    PurchaseRequestResponseDto rejectPurchaseRequest(
            Long id,
            Long authenticatedUserId);

    PurchaseRequestResponseDto cancelPurchaseRequest(
            Long id,
            Long authenticatedUserId);
}