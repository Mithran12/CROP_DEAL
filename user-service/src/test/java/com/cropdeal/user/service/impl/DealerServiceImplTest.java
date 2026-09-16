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

import com.cropdeal.user.dto.request.DealerRequest;
import com.cropdeal.user.dto.response.DealerResponse;
import com.cropdeal.user.entity.Dealer;
import com.cropdeal.user.exception.DealerNotFoundException;
import com.cropdeal.user.exception.DuplicateResourceException;
import com.cropdeal.user.mapper.DealerMapper;
import com.cropdeal.user.repository.DealerRepository;
import com.cropdeal.user.security.UserAuthorizationService;

@ExtendWith(MockitoExtension.class)
class DealerServiceImplTest {

    @Mock
    private DealerRepository dealerRepository;

    @Mock
    private DealerMapper dealerMapper;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @InjectMocks
    private DealerServiceImpl dealerService;

    private DealerRequest dealerRequest;
    private Dealer dealer;
    private DealerResponse dealerResponse;

    @BeforeEach
    void setUp() {

        dealerRequest = new DealerRequest();
        dealerRequest.setAuthUserId(1L);
        dealerRequest.setBusinessName("ABC Traders");
        dealerRequest.setContactPersonName("John Dealer");
        dealerRequest.setEmail("dealer@gmail.com");
        dealerRequest.setPhone("9876543210");

        dealer = Dealer.builder()
                .dealerId(1L)
                .authUserId(1L)
                .businessName("ABC Traders")
                .contactPersonName("John Dealer")
                .email("dealer@gmail.com")
                .phone("9876543210")
                .build();

        dealerResponse = DealerResponse.builder()
                .dealerId(1L)
                .authUserId(1L)
                .businessName("ABC Traders")
                .contactPersonName("John Dealer")
                .email("dealer@gmail.com")
                .phone("9876543210")
                .build();
    }

    @Test
    void createDealer_shouldCreateSuccessfully() {

        when(dealerRepository.existsByAuthUserId(1L)).thenReturn(false);

        when(dealerRepository.existsByEmail("dealer@gmail.com")).thenReturn(false);

        when(dealerMapper.toEntity(dealerRequest)).thenReturn(dealer);

        when(dealerRepository.save(dealer)).thenReturn(dealer);

        when(dealerMapper.toResponse(dealer)).thenReturn(dealerResponse);

        DealerResponse result = dealerService.createDealer(dealerRequest);

        assertEquals(1L, result.getDealerId());
        assertEquals("ABC Traders", result.getBusinessName());
        assertEquals("dealer@gmail.com", result.getEmail());

        verify(dealerRepository).save(dealer);
    }

    @Test
    void createDealer_shouldThrowExceptionWhenAuthUserAlreadyExists() {

        when(dealerRepository.existsByAuthUserId(1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,() -> dealerService.createDealer(dealerRequest));
    
        verify(dealerRepository, never()).save(any(Dealer.class));
    }

    @Test
    void createDealer_shouldThrowExceptionWhenEmailAlreadyExists() {

        when(dealerRepository.existsByAuthUserId(1L)).thenReturn(false);

        when(dealerRepository.existsByEmail("dealer@gmail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,() -> dealerService.createDealer(dealerRequest));
       

        verify(dealerRepository, never()).save(any(Dealer.class));
    }

    @Test
    void getDealerById_shouldReturnDealer() {

        when(dealerRepository.findById(1L)).thenReturn(Optional.of(dealer));

        when(userAuthorizationService.canAccess(1L)).thenReturn(true);

        when(dealerMapper.toResponse(dealer)).thenReturn(dealerResponse);

        DealerResponse result = dealerService.getDealerById(1L);

        assertEquals(1L, result.getDealerId());
        assertEquals("ABC Traders", result.getBusinessName());

        verify(dealerRepository).findById(1L);
    }

    @Test
    void getDealerById_shouldThrowExceptionWhenDealerNotFound() {

        when(dealerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DealerNotFoundException.class, () -> dealerService.getDealerById(1L));
        
    }
}