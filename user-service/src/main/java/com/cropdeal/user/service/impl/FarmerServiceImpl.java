package com.cropdeal.user.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cropdeal.user.dto.request.FarmerRequest;
import com.cropdeal.user.dto.response.FarmerResponse;
import com.cropdeal.user.entity.Farmer;
import com.cropdeal.user.entity.UserStatus;
import com.cropdeal.user.exception.DuplicateResourceException;
import com.cropdeal.user.exception.FarmerNotFoundException;
import com.cropdeal.user.mapper.FarmerMapper;
import com.cropdeal.user.repository.FarmerRepository;
import com.cropdeal.user.security.UserAuthorizationService;
import com.cropdeal.user.service.FarmerService;

@Service
@Transactional
public class FarmerServiceImpl implements FarmerService {

    private static final Logger log = LoggerFactory.getLogger(FarmerServiceImpl.class);

    private final FarmerRepository farmerRepository;
    private final FarmerMapper farmerMapper;
    private final UserAuthorizationService userAuthorizationService;

    public FarmerServiceImpl(FarmerRepository farmerRepository, FarmerMapper farmerMapper, UserAuthorizationService userAuthorizationService) {
        this.farmerRepository = farmerRepository;
        this.farmerMapper = farmerMapper;
        this.userAuthorizationService = userAuthorizationService;
    }

    @Override
    public FarmerResponse createFarmer(FarmerRequest request) {

        log.info("Creating farmer for authUserId: {}", request.getAuthUserId());

        if (farmerRepository.existsByAuthUserId(request.getAuthUserId())) {
            throw new DuplicateResourceException("Farmer already exists for this auth user");
        }

        if (farmerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Farmer already exists with email: " + request.getEmail());
        }

        Farmer farmer = farmerMapper.toEntity(request);
        farmer.setStatus(UserStatus.ACTIVE);

        Farmer savedFarmer = farmerRepository.save(farmer);

        log.info("Farmer created successfully with id: {}", savedFarmer.getFarmerId());

        return farmerMapper.toResponse(savedFarmer);
    }

    @Override
    @Transactional(readOnly = true)
    public FarmerResponse getFarmerById(Long farmerId) {

        log.info("Fetching farmer with id: {}", farmerId);

        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException("Farmer not found with id: " + farmerId));

        if (!userAuthorizationService.canAccess(farmer.getAuthUserId())) {
            throw new AccessDeniedException("You are not authorized to access this farmer profile");
        }

        return farmerMapper.toResponse(farmer);
    }

    @Override
    @Transactional(readOnly = true)
    public FarmerResponse getFarmerByAuthUserId(Long authUserId) {

        log.info("Fetching farmer with authUserId: {}", authUserId);

        if (!userAuthorizationService.canAccess(authUserId)) {
            throw new AccessDeniedException("You are not authorized to access this farmer profile");
        }

        Farmer farmer = farmerRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new FarmerNotFoundException("Farmer not found for auth user id: " + authUserId));

        return farmerMapper.toResponse(farmer);
    }

    @Override
    public FarmerResponse updateFarmer(Long farmerId, FarmerRequest request) {

        log.info("Updating farmer with id: {}", farmerId);

        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException("Farmer not found with id: " + farmerId));

        if (!userAuthorizationService.canAccess(farmer.getAuthUserId())) {
            throw new AccessDeniedException("You are not authorized to update this farmer profile");
        }

        if (!farmer.getEmail().equals(request.getEmail()) && farmerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        farmer.setFullName(request.getFullName());
        farmer.setEmail(request.getEmail());
        farmer.setPhone(request.getPhone());
        farmer.setFarmName(request.getFarmName());
        farmer.setFarmSize(request.getFarmSize());
        farmer.setFarmSizeUnit(request.getFarmSizeUnit());
        farmer.setAddressLine(request.getAddressLine());
        farmer.setCity(request.getCity());
        farmer.setState(request.getState());
        farmer.setPincode(request.getPincode());
        farmer.setBankAccountNumber(request.getBankAccountNumber());
        farmer.setIfscCode(request.getIfscCode());
        farmer.setBankName(request.getBankName());

        Farmer updatedFarmer = farmerRepository.save(farmer);

        log.info("Farmer updated successfully with id: {}", farmerId);

        return farmerMapper.toResponse(updatedFarmer);
    }

    @Override
    public FarmerResponse activateFarmer(Long farmerId) {

        log.info("Activating farmer with id: {}", farmerId);

        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException("Farmer not found with id: " + farmerId));

        farmer.setStatus(UserStatus.ACTIVE);

        Farmer updatedFarmer = farmerRepository.save(farmer);

        log.info("Farmer activated successfully with id: {}", farmerId);

        return farmerMapper.toResponse(updatedFarmer);
    }

    @Override
    public FarmerResponse deactivateFarmer(Long farmerId) {

        log.info("Deactivating farmer with id: {}", farmerId);

        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException("Farmer not found with id: " + farmerId));

        farmer.setStatus(UserStatus.INACTIVE);

        Farmer updatedFarmer = farmerRepository.save(farmer);

        log.info("Farmer deactivated successfully with id: {}", farmerId);

        return farmerMapper.toResponse(updatedFarmer);
    }

    @Override
    public void deleteFarmer(Long farmerId) {

        log.info("Deleting farmer with id: {}", farmerId);

        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException("Farmer not found with id: " + farmerId));

        farmerRepository.delete(farmer);

        log.info("Farmer deleted successfully with id: {}", farmerId);
    }
}