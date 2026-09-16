package com.cropdeal.user.mapper;

import org.springframework.stereotype.Component;

import com.cropdeal.user.dto.request.FarmerRequest;
import com.cropdeal.user.dto.response.FarmerResponse;
import com.cropdeal.user.entity.Farmer;

@Component
public class FarmerMapper {

    public Farmer toEntity(FarmerRequest request) {

        return Farmer.builder()
                .authUserId(request.getAuthUserId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .farmName(request.getFarmName())
                .farmSize(request.getFarmSize())
                .farmSizeUnit(request.getFarmSizeUnit())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .bankAccountNumber(request.getBankAccountNumber())
                .ifscCode(request.getIfscCode())
                .bankName(request.getBankName())
                .build();
    }

    public FarmerResponse toResponse(Farmer farmer) {

        return FarmerResponse.builder()
                .farmerId(farmer.getFarmerId())
                .authUserId(farmer.getAuthUserId())
                .fullName(farmer.getFullName())
                .email(farmer.getEmail())
                .phone(farmer.getPhone())
                .farmName(farmer.getFarmName())
                .farmSize(farmer.getFarmSize())
                .farmSizeUnit(farmer.getFarmSizeUnit())
                .addressLine(farmer.getAddressLine())
                .city(farmer.getCity())
                .state(farmer.getState())
                .pincode(farmer.getPincode())
                .bankAccountNumber(farmer.getBankAccountNumber())
                .ifscCode(farmer.getIfscCode())
                .bankName(farmer.getBankName())
                .status(farmer.getStatus())
                .createdAt(farmer.getCreatedAt())
                .updatedAt(farmer.getUpdatedAt())
                .build();
    }
}