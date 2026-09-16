package com.cropdeal.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cropdeal.user.dto.request.FarmerRequest;
import com.cropdeal.user.dto.response.FarmerResponse;
import com.cropdeal.user.entity.Farmer;
import com.cropdeal.user.exception.DuplicateResourceException;
import com.cropdeal.user.exception.FarmerNotFoundException;
import com.cropdeal.user.mapper.FarmerMapper;
import com.cropdeal.user.repository.FarmerRepository;
import com.cropdeal.user.security.UserAuthorizationService;

@ExtendWith(MockitoExtension.class)
class FarmerServiceImplTest {

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private FarmerMapper farmerMapper;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @InjectMocks
    private FarmerServiceImpl farmerService;

    private FarmerRequest farmerRequest;
    private Farmer farmer;
    private FarmerResponse farmerResponse;

    @BeforeEach
    void setUp() {

        farmerRequest = new FarmerRequest();
        farmerRequest.setAuthUserId(1L);
        farmerRequest.setFullName("John Farmer");
        farmerRequest.setEmail("john@gmail.com");
        farmerRequest.setPhone("9876543210");

        farmer = Farmer.builder()
                .farmerId(1L)
                .authUserId(1L)
                .fullName("John Farmer")
                .email("john@gmail.com")
                .phone("9876543210")
                .build();

        farmerResponse = FarmerResponse.builder()
                .farmerId(1L)
                .authUserId(1L)
                .fullName("John Farmer")
                .email("john@gmail.com")
                .phone("9876543210")
                .build();
    }

    @Test
    void createFarmer_shouldCreateSuccessfully() {

        when(farmerRepository.existsByAuthUserId(1L)).thenReturn(false);

        when(farmerRepository.existsByEmail("john@gmail.com")).thenReturn(false);

        when(farmerMapper.toEntity(farmerRequest)).thenReturn(farmer);

        when(farmerRepository.save(farmer)).thenReturn(farmer);

        when(farmerMapper.toResponse(farmer)).thenReturn(farmerResponse);

        FarmerResponse result = farmerService.createFarmer(farmerRequest);

        assertEquals(1L, result.getFarmerId());
        assertEquals("John Farmer", result.getFullName());
        assertEquals("john@gmail.com", result.getEmail());

        verify(farmerRepository).save(farmer);
    }

    @Test
    void createFarmer_shouldThrowExceptionWhenAuthUserAlreadyExists() {

        when(farmerRepository.existsByAuthUserId(1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> farmerService.createFarmer(farmerRequest));
        
        verify(farmerRepository, never()).save(any(Farmer.class));
    }

    @Test
    void createFarmer_shouldThrowExceptionWhenEmailAlreadyExists() {

        when(farmerRepository.existsByAuthUserId(1L)).thenReturn(false);

        when(farmerRepository.existsByEmail("john@gmail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,() -> farmerService.createFarmer(farmerRequest));
   
        verify(farmerRepository, never()).save(any(Farmer.class));
    }

    @Test
    void getFarmerById_shouldReturnFarmer() {

        when(farmerRepository.findById(1L)).thenReturn(Optional.of(farmer));

        when(userAuthorizationService.canAccess(1L)).thenReturn(true);

        when(farmerMapper.toResponse(farmer)).thenReturn(farmerResponse);

        FarmerResponse result = farmerService.getFarmerById(1L);

        assertEquals(1L, result.getFarmerId());
        assertEquals("John Farmer", result.getFullName());

        verify(farmerRepository).findById(1L);
    }

    @Test
    void getFarmerById_shouldThrowExceptionWhenFarmerNotFound() {

        when(farmerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(FarmerNotFoundException.class,() -> farmerService.getFarmerById(1L));

    }
}