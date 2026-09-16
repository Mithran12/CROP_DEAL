package com.cropdeal.crop.service;

import java.util.List;

import com.cropdeal.crop.dto.request.CropRequest;
import com.cropdeal.crop.dto.response.CropResponse;
import com.cropdeal.crop.entity.CropCategory;

public interface CropService {

    CropResponse createCrop(CropRequest request);

    CropResponse getCropById(Long cropId);

    List<CropResponse> getAllCrops();

    List<CropResponse> getCropsByFarmer(Long farmerId);

    CropResponse updateCrop(Long cropId, CropRequest request);

    CropResponse publishCrop(Long cropId);

    CropResponse unpublishCrop(Long cropId);

    CropResponse updateQuantity(Long cropId, Double quantity);

    void deleteCrop(Long cropId);

    List<CropResponse> searchCrops(String cropName);

    List<CropResponse> getCropsByCategory(CropCategory category);

    List<CropResponse> getCropsByCity(String city);
}