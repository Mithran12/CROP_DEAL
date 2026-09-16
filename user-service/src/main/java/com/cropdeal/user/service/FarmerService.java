package com.cropdeal.user.service;

import com.cropdeal.user.dto.request.FarmerRequest;
import com.cropdeal.user.dto.response.FarmerResponse;

public interface FarmerService {

    FarmerResponse createFarmer(FarmerRequest request);

    FarmerResponse getFarmerById(Long farmerId);

    FarmerResponse getFarmerByAuthUserId(Long authUserId);

    FarmerResponse updateFarmer(Long farmerId, FarmerRequest request);

    FarmerResponse activateFarmer(Long farmerId);

    FarmerResponse deactivateFarmer(Long farmerId);

    void deleteFarmer(Long farmerId);
}