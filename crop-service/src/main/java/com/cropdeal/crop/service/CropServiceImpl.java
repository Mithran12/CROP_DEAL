package com.cropdeal.crop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cropdeal.crop.dto.event.CropPublishedEvent;
import com.cropdeal.crop.dto.request.CropRequest;
import com.cropdeal.crop.dto.response.CropResponse;
import com.cropdeal.crop.entity.Crop;
import com.cropdeal.crop.entity.CropCategory;
import com.cropdeal.crop.entity.CropStatus;
import com.cropdeal.crop.exception.CropAccessDeniedException;
import com.cropdeal.crop.exception.CropNotFoundException;
import com.cropdeal.crop.mapper.CropMapper;
import com.cropdeal.crop.messaging.RabbitMQProducer;
import com.cropdeal.crop.repository.CropRepository;
import com.cropdeal.crop.security.UserAuthorizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CropServiceImpl implements CropService {

    private final CropRepository cropRepository;
    private final CropMapper cropMapper;
    private final UserAuthorizationService userAuthorizationService;
    private final RabbitMQProducer rabbitMQProducer;

    @Override
    public CropResponse createCrop(CropRequest request) {

        log.info("Creating crop: {}", request.getCropName());

        Crop crop = cropMapper.toEntity(request);

        crop.setStatus(CropStatus.UNPUBLISHED);

        Crop savedCrop = cropRepository.save(crop);

        log.info("Crop created successfully with ID: {}", savedCrop.getCropId());
       

        return cropMapper.toResponse(savedCrop);
    }

    @Override
    @Transactional(readOnly = true)
    public CropResponse getCropById(Long cropId) {

        Crop crop = findCropById(cropId);

        return cropMapper.toResponse(crop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CropResponse> getAllCrops() {

        return cropRepository.findAll()
                .stream()
                .map(cropMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CropResponse> getCropsByFarmer(Long farmerId) {

        return cropRepository.findByFarmerId(farmerId)
                .stream()
                .map(cropMapper::toResponse)
                .toList();
    }

    @Override
    public CropResponse updateCrop(Long cropId, CropRequest request) {

        Crop crop = findCropById(cropId);

        if (!userAuthorizationService.canAccess(crop.getFarmerId())) {
            throw new CropAccessDeniedException("You are not authorized to modify this crop");
            
        }

        crop.setCropName(request.getCropName());
        crop.setCategory(request.getCategory());
        crop.setDescription(request.getDescription());
        crop.setQuantity(request.getQuantity());
        crop.setQuantityUnit(request.getQuantityUnit());
        crop.setExpectedPrice(request.getExpectedPrice());
        crop.setQuality(request.getQuality());
        crop.setHarvestDate(request.getHarvestDate());
        crop.setAddressLine(request.getAddressLine());
        crop.setCity(request.getCity());
        crop.setState(request.getState());
        crop.setPincode(request.getPincode());
        crop.setImageUrl(request.getImageUrl());

        Crop updatedCrop = cropRepository.save(crop);

        log.info("Crop updated successfully with ID: {}",cropId);
        

        return cropMapper.toResponse(updatedCrop);
    }

    @Override
    public CropResponse publishCrop(Long cropId) {

        Crop crop = findCropById(cropId);

        if (!userAuthorizationService.canAccess(crop.getFarmerId())) {
            throw new CropAccessDeniedException("You are not authorized to publish this crop");
            
        }

        crop.setStatus(CropStatus.PUBLISHED);

        Crop updatedCrop = cropRepository.save(crop);

        CropPublishedEvent event = CropPublishedEvent.builder()
                .cropId(updatedCrop.getCropId())
                .farmerId(updatedCrop.getFarmerId())
                .cropName(updatedCrop.getCropName())
                .category(updatedCrop.getCategory().name())
                .quantity(updatedCrop.getQuantity())
                .quantityUnit(updatedCrop.getQuantityUnit())
                .expectedPrice(updatedCrop.getExpectedPrice())
                .quality(updatedCrop.getQuality().name())
                .city(updatedCrop.getCity())
                .state(updatedCrop.getState())
                .build();

        rabbitMQProducer.sendCropPublishedEvent(event);

        log.info("Crop published successfully with ID: {}",cropId);
       

        return cropMapper.toResponse(updatedCrop);
    }

    @Override
    public CropResponse unpublishCrop(Long cropId) {

        Crop crop = findCropById(cropId);

        if (!userAuthorizationService.canAccess(crop.getFarmerId())) {
            throw new CropAccessDeniedException("You are not authorized to unpublish this crop");
            
        }

        crop.setStatus(CropStatus.UNPUBLISHED);

        Crop updatedCrop = cropRepository.save(crop);

        log.info("Crop unpublished successfully with ID: {}",cropId);
      
        return cropMapper.toResponse(updatedCrop);
    }

    @Override
    public CropResponse updateQuantity(Long cropId, Double quantity) {

        Crop crop = findCropById(cropId);

        if (!userAuthorizationService.canAccess(crop.getFarmerId())) {
            throw new CropAccessDeniedException("You are not authorized to update this crop");
            
        }

        crop.setQuantity(quantity);

        Crop updatedCrop = cropRepository.save(crop);

        log.info("Quantity updated for crop ID: {} to {}",cropId,quantity);
       

        return cropMapper.toResponse(updatedCrop);
    }

    @Override
    public void deleteCrop(Long cropId) {

        Crop crop = findCropById(cropId);

        if (!userAuthorizationService.canAccess(crop.getFarmerId())) {
            throw new CropAccessDeniedException("You are not authorized to delete this crop");
            
        }

        cropRepository.delete(crop);

        log.info("Crop deleted successfully with ID: {}", cropId);
        
    }

    @Override
    @Transactional(readOnly = true)
    public List<CropResponse> searchCrops(String cropName) {

        return cropRepository
                .findByCropNameContainingIgnoreCase(cropName)
                .stream()
                .map(cropMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CropResponse> getCropsByCategory(CropCategory category) {

        return cropRepository
                .findByCategory(category)
                .stream()
                .map(cropMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CropResponse> getCropsByCity(String city) {

        return cropRepository
                .findByCityIgnoreCase(city)
                .stream()
                .map(cropMapper::toResponse)
                .toList();
    }

    private Crop findCropById(Long cropId) {

        return cropRepository.findById(cropId)
                .orElseThrow(() ->
                        new CropNotFoundException("Crop not found with ID: " + cropId));   
    }
}