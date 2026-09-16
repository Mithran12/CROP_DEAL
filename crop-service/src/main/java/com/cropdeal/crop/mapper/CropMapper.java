package com.cropdeal.crop.mapper;

import org.springframework.stereotype.Component;

import com.cropdeal.crop.dto.request.CropRequest;
import com.cropdeal.crop.dto.response.CropResponse;
import com.cropdeal.crop.entity.Crop;

@Component
public class CropMapper {

    public Crop toEntity(CropRequest request) {

        return Crop.builder()
                .farmerId(request.getFarmerId())
                .cropName(request.getCropName())
                .category(request.getCategory())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .quantityUnit(request.getQuantityUnit())
                .expectedPrice(request.getExpectedPrice())
                .quality(request.getQuality())
                .harvestDate(request.getHarvestDate())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .imageUrl(request.getImageUrl())
                .build();
    }

    public CropResponse toResponse(Crop crop) {

        return CropResponse.builder()
                .cropId(crop.getCropId())
                .farmerId(crop.getFarmerId())
                .cropName(crop.getCropName())
                .category(crop.getCategory())
                .description(crop.getDescription())
                .quantity(crop.getQuantity())
                .quantityUnit(crop.getQuantityUnit())
                .expectedPrice(crop.getExpectedPrice())
                .quality(crop.getQuality())
                .harvestDate(crop.getHarvestDate())
                .addressLine(crop.getAddressLine())
                .city(crop.getCity())
                .state(crop.getState())
                .pincode(crop.getPincode())
                .imageUrl(crop.getImageUrl())
                .status(crop.getStatus())
                .createdAt(crop.getCreatedAt())
                .updatedAt(crop.getUpdatedAt())
                .build();
    }
}