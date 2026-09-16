package com.cropdeal.crop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cropdeal.crop.dto.request.CropRequest;
import com.cropdeal.crop.dto.response.CropResponse;
import com.cropdeal.crop.entity.Crop;
import com.cropdeal.crop.entity.CropCategory;
import com.cropdeal.crop.entity.CropQuality;
import com.cropdeal.crop.entity.CropStatus;
import com.cropdeal.crop.mapper.CropMapper;
import com.cropdeal.crop.messaging.RabbitMQProducer;
import com.cropdeal.crop.repository.CropRepository;
import com.cropdeal.crop.security.UserAuthorizationService;

@ExtendWith(MockitoExtension.class)
class CropServiceImplTest {

    @Mock
    private CropRepository cropRepository;

    @Mock
    private CropMapper cropMapper;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @InjectMocks
    private CropServiceImpl cropService;

    private Crop crop;
    private CropRequest request;
    private CropResponse response;

    @BeforeEach
    void setUp() {

        crop = Crop.builder()
                .cropId(1L)
                .farmerId(1L)
                .cropName("Rice")
                .category(CropCategory.CEREALS)
                .quantity(500.0)
                .quantityUnit("KG")
                .expectedPrice(new BigDecimal("45.00"))
                .quality(CropQuality.A)
                .city("Coimbatore")
                .state("Tamil Nadu")
                .status(CropStatus.UNPUBLISHED)
                .build();

        request = CropRequest.builder()
                .farmerId(1L)
                .cropName("Rice")
                .category(CropCategory.CEREALS)
                .quantity(500.0)
                .quantityUnit("KG")
                .expectedPrice(new BigDecimal("45.00"))
                .quality(CropQuality.A)
                .city("Coimbatore")
                .state("Tamil Nadu")
                .pincode("641001")
                .build();

        response = CropResponse.builder()
                .cropId(1L)
                .farmerId(1L)
                .cropName("Rice")
                .category(CropCategory.CEREALS)
                .quantity(500.0)
                .quantityUnit("KG")
                .expectedPrice(new BigDecimal("45.00"))
                .quality(CropQuality.A)
                .city("Coimbatore")
                .state("Tamil Nadu")
                .status(CropStatus.UNPUBLISHED)
                .build();
    }

    @Test
    void createCrop_shouldCreateCropSuccessfully() {

        when(cropMapper.toEntity(request)).thenReturn(crop);

        when(cropRepository.save(crop)).thenReturn(crop);

        when(cropMapper.toResponse(crop)).thenReturn(response);

        CropResponse result = cropService.createCrop(request);

        assertNotNull(result);
        assertEquals(1L, result.getCropId());
        assertEquals("Rice", result.getCropName());
        assertEquals(CropStatus.UNPUBLISHED, result.getStatus()
        );

        verify(cropRepository).save(crop);
    }

    @Test
    void getCropById_shouldReturnCrop() {

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));

        when(cropMapper.toResponse(crop)).thenReturn(response);

        CropResponse result = cropService.getCropById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getCropId());
        assertEquals("Rice", result.getCropName());

        verify(cropRepository).findById(1L);
    }

    @Test
    void publishCrop_shouldPublishSuccessfully() {

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));

        when(userAuthorizationService.canAccess(1L)).thenReturn(true);

        when(cropRepository.save(crop)).thenReturn(crop);

        when(cropMapper.toResponse(crop)).thenReturn(response);

        CropResponse result = cropService.publishCrop(1L);

        assertNotNull(result);

        assertEquals(CropStatus.PUBLISHED,crop.getStatus());
        

        verify(rabbitMQProducer).sendCropPublishedEvent(any());
    }

    @Test
    void unpublishCrop_shouldUnpublishSuccessfully() {

        crop.setStatus(CropStatus.PUBLISHED);

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));

        when(userAuthorizationService.canAccess(1L)).thenReturn(true);

        when(cropRepository.save(crop)).thenReturn(crop);

        when(cropMapper.toResponse(crop)).thenReturn(response);

        CropResponse result = cropService.unpublishCrop(1L);

        assertNotNull(result);

        assertEquals(CropStatus.UNPUBLISHED, crop.getStatus());
        

        verify(cropRepository).save(crop);
    }

    @Test
    void updateQuantity_shouldUpdateSuccessfully() {

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));

        when(userAuthorizationService.canAccess(1L)).thenReturn(true);

        when(cropRepository.save(crop)).thenReturn(crop);

        when(cropMapper.toResponse(crop)).thenReturn(response);

        CropResponse result = cropService.updateQuantity(1L, 750.0);

        assertNotNull(result);
        assertEquals(750.0, crop.getQuantity());

        verify(cropRepository).save(crop);
    }

    @Test
    void deleteCrop_shouldDeleteSuccessfully() {

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));

        when(userAuthorizationService.canAccess(1L)).thenReturn(true);

        cropService.deleteCrop(1L);

        verify(cropRepository).delete(crop);
    }
}