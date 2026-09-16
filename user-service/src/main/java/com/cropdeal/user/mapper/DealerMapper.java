package com.cropdeal.user.mapper;

import org.springframework.stereotype.Component;

import com.cropdeal.user.dto.request.DealerRequest;
import com.cropdeal.user.dto.response.DealerResponse;
import com.cropdeal.user.entity.Dealer;

@Component
public class DealerMapper {

    public Dealer toEntity(DealerRequest request) {

        return Dealer.builder()
                .authUserId(request.getAuthUserId())
                .businessName(request.getBusinessName())
                .contactPersonName(request.getContactPersonName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .businessType(request.getBusinessType())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .bankAccountNumber(request.getBankAccountNumber())
                .ifscCode(request.getIfscCode())
                .bankName(request.getBankName())
                .build();
    }

    public DealerResponse toResponse(Dealer dealer) {

        return DealerResponse.builder()
                .dealerId(dealer.getDealerId())
                .authUserId(dealer.getAuthUserId())
                .businessName(dealer.getBusinessName())
                .contactPersonName(dealer.getContactPersonName())
                .email(dealer.getEmail())
                .phone(dealer.getPhone())
                .businessType(dealer.getBusinessType())
                .addressLine(dealer.getAddressLine())
                .city(dealer.getCity())
                .state(dealer.getState())
                .pincode(dealer.getPincode())
                .bankAccountNumber(dealer.getBankAccountNumber())
                .ifscCode(dealer.getIfscCode())
                .bankName(dealer.getBankName())
                .status(dealer.getStatus())
                .createdAt(dealer.getCreatedAt())
                .updatedAt(dealer.getUpdatedAt())
                .build();
    }
}