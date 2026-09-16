package com.cropdeal.user.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cropdeal.user.dto.request.DealerRequest;
import com.cropdeal.user.dto.response.DealerResponse;
import com.cropdeal.user.entity.Dealer;
import com.cropdeal.user.entity.UserStatus;
import com.cropdeal.user.exception.DealerNotFoundException;
import com.cropdeal.user.exception.DuplicateResourceException;
import com.cropdeal.user.mapper.DealerMapper;
import com.cropdeal.user.repository.DealerRepository;
import com.cropdeal.user.security.UserAuthorizationService;
import com.cropdeal.user.service.DealerService;

@Service
@Transactional
public class DealerServiceImpl implements DealerService {

    private static final Logger log = LoggerFactory.getLogger(DealerServiceImpl.class);

    private final DealerRepository dealerRepository;
    private final DealerMapper dealerMapper;
    private final UserAuthorizationService userAuthorizationService;

    public DealerServiceImpl(DealerRepository dealerRepository, DealerMapper dealerMapper, UserAuthorizationService userAuthorizationService) {
        this.dealerRepository = dealerRepository;
        this.dealerMapper = dealerMapper;
        this.userAuthorizationService = userAuthorizationService;
    }

    @Override
    public DealerResponse createDealer(DealerRequest request) {

        log.info("Creating dealer for authUserId: {}", request.getAuthUserId());

        if (dealerRepository.existsByAuthUserId(request.getAuthUserId())) {
            throw new DuplicateResourceException("Dealer already exists for this auth user");
        }

        if (dealerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Dealer already exists with email: " + request.getEmail());
        }

        Dealer dealer = dealerMapper.toEntity(request);
        dealer.setStatus(UserStatus.ACTIVE);

        Dealer savedDealer = dealerRepository.save(dealer);

        log.info("Dealer created successfully with id: {}", savedDealer.getDealerId());

        return dealerMapper.toResponse(savedDealer);
    }

    @Override
    @Transactional(readOnly = true)
    public DealerResponse getDealerById(Long dealerId) {

        log.info("Fetching dealer with id: {}", dealerId);

        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new DealerNotFoundException("Dealer not found with id: " + dealerId));

        if (!userAuthorizationService.canAccess(dealer.getAuthUserId())) {
            throw new AccessDeniedException("You are not authorized to access this dealer profile");
        }

        return dealerMapper.toResponse(dealer);
    }

    @Override
    @Transactional(readOnly = true)
    public DealerResponse getDealerByAuthUserId(Long authUserId) {

        log.info("Fetching dealer with authUserId: {}", authUserId);

        if (!userAuthorizationService.canAccess(authUserId)) {
            throw new AccessDeniedException("You are not authorized to access this dealer profile");
        }

        Dealer dealer = dealerRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new DealerNotFoundException("Dealer not found for auth user id: " + authUserId));

        return dealerMapper.toResponse(dealer);
    }

    @Override
    public DealerResponse updateDealer(Long dealerId, DealerRequest request) {

        log.info("Updating dealer with id: {}", dealerId);

        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new DealerNotFoundException("Dealer not found with id: " + dealerId));

        if (!userAuthorizationService.canAccess(dealer.getAuthUserId())) {
            throw new AccessDeniedException("You are not authorized to update this dealer profile");
        }

        if (!dealer.getEmail().equals(request.getEmail()) && dealerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        dealer.setBusinessName(request.getBusinessName());
        dealer.setContactPersonName(request.getContactPersonName());
        dealer.setEmail(request.getEmail());
        dealer.setPhone(request.getPhone());
        dealer.setBusinessType(request.getBusinessType());
        dealer.setAddressLine(request.getAddressLine());
        dealer.setCity(request.getCity());
        dealer.setState(request.getState());
        dealer.setPincode(request.getPincode());
        dealer.setBankAccountNumber(request.getBankAccountNumber());
        dealer.setIfscCode(request.getIfscCode());
        dealer.setBankName(request.getBankName());

        Dealer updatedDealer = dealerRepository.save(dealer);

        log.info("Dealer updated successfully with id: {}", dealerId);

        return dealerMapper.toResponse(updatedDealer);
    }

    @Override
    public DealerResponse activateDealer(Long dealerId) {

        log.info("Activating dealer with id: {}", dealerId);

        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new DealerNotFoundException("Dealer not found with id: " + dealerId));

        dealer.setStatus(UserStatus.ACTIVE);

        Dealer updatedDealer = dealerRepository.save(dealer);

        log.info("Dealer activated successfully with id: {}", dealerId);

        return dealerMapper.toResponse(updatedDealer);
    }

    @Override
    public DealerResponse deactivateDealer(Long dealerId) {

        log.info("Deactivating dealer with id: {}", dealerId);

        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new DealerNotFoundException("Dealer not found with id: " + dealerId));

        dealer.setStatus(UserStatus.INACTIVE);

        Dealer updatedDealer = dealerRepository.save(dealer);

        log.info("Dealer deactivated successfully with id: {}", dealerId);

        return dealerMapper.toResponse(updatedDealer);
    }

    @Override
    public void deleteDealer(Long dealerId) {

        log.info("Deleting dealer with id: {}", dealerId);

        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new DealerNotFoundException("Dealer not found with id: " + dealerId));

        dealerRepository.delete(dealer);

        log.info("Dealer deleted successfully with id: {}", dealerId);
    }
}