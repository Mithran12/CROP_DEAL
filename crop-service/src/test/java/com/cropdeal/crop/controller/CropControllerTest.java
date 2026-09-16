package com.cropdeal.crop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cropdeal.crop.dto.request.CropRequest;
import com.cropdeal.crop.dto.response.CropResponse;
import com.cropdeal.crop.entity.CropCategory;
import com.cropdeal.crop.entity.CropQuality;
import com.cropdeal.crop.entity.CropStatus;
import com.cropdeal.crop.exception.GlobalExceptionHandler;
import com.cropdeal.crop.service.CropService;
import com.fasterxml.jackson.databind.ObjectMapper;

class CropControllerTest {

    private MockMvc mockMvc;

    private CropService cropService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        cropService = mock(CropService.class);

        objectMapper = new ObjectMapper();

        objectMapper.findAndRegisterModules();

        CropController cropController =
                new CropController(cropService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(cropController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createCrop_shouldReturn201() throws Exception {

        CropRequest request = CropRequest.builder()
                .farmerId(1L)
                .cropName("Rice")
                .category(CropCategory.CEREALS)
                .quantity(500.0)
                .quantityUnit("KG")
                .expectedPrice(new BigDecimal("45.00"))
                .quality(CropQuality.A)
                .harvestDate(LocalDate.of(2026, 10, 15))
                .addressLine("Main Road")
                .city("Coimbatore")
                .state("Tamil Nadu")
                .pincode("641001")
                .imageUrl("https://example.com/rice.jpg")
                .build();

        CropResponse response = CropResponse.builder()
                .cropId(1L)
                .farmerId(1L)
                .cropName("Rice")
                .category(CropCategory.CEREALS)
                .quantity(500.0)
                .quantityUnit("KG")
                .expectedPrice(new BigDecimal("45.00"))
                .quality(CropQuality.A)
                .status(CropStatus.UNPUBLISHED)
                .build();

        when(cropService.createCrop(any(CropRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/crops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isCreated());
    }

    @Test
    void createCrop_withInvalidRequest_shouldReturn400()
            throws Exception {

        CropRequest request = CropRequest.builder()
                .farmerId(null)
                .cropName("")
                .category(null)
                .quantity(null)
                .quantityUnit("")
                .expectedPrice(null)
                .quality(null)
                .city("")
                .state("")
                .pincode("")
                .build();

        mockMvc.perform(
                post("/api/crops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void getCropById_shouldReturn200() throws Exception {

        CropResponse response = CropResponse.builder()
                .cropId(1L)
                .farmerId(1L)
                .cropName("Rice")
                .category(CropCategory.CEREALS)
                .quantity(500.0)
                .quantityUnit("KG")
                .expectedPrice(new BigDecimal("45.00"))
                .quality(CropQuality.A)
                .status(CropStatus.PUBLISHED)
                .build();

        when(cropService.getCropById(1L))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/crops/1")
        )
        .andExpect(status().isOk());
    }

    @Test
    void publishCrop_shouldReturn200() throws Exception {

        CropResponse response = CropResponse.builder()
                .cropId(1L)
                .farmerId(1L)
                .cropName("Rice")
                .status(CropStatus.PUBLISHED)
                .build();

        when(cropService.publishCrop(1L))
                .thenReturn(response);

        mockMvc.perform(
                patch("/api/crops/1/publish")
        )
        .andExpect(status().isOk());
    }

    @Test
    void unpublishCrop_shouldReturn200() throws Exception {

        CropResponse response = CropResponse.builder()
                .cropId(1L)
                .farmerId(1L)
                .cropName("Rice")
                .status(CropStatus.UNPUBLISHED)
                .build();

        when(cropService.unpublishCrop(1L))
                .thenReturn(response);

        mockMvc.perform(
                patch("/api/crops/1/unpublish")
        )
        .andExpect(status().isOk());
    }

    @Test
    void deleteCrop_shouldReturn204() throws Exception {

        mockMvc.perform(
                delete("/api/crops/1")
        )
        .andExpect(status().isNoContent());
    }
}