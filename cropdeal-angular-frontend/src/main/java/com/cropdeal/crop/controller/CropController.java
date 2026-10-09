package com.cropdeal.crop.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cropdeal.crop.dto.request.CropRequest;
import com.cropdeal.crop.dto.response.CropResponse;
import com.cropdeal.crop.entity.CropCategory;
import com.cropdeal.crop.service.CropService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/crops")
@RequiredArgsConstructor
public class CropController {

    private final CropService cropService;

    @PostMapping
    public ResponseEntity<CropResponse> createCrop(
            @Valid @RequestBody CropRequest request) {

        CropResponse response = cropService.createCrop(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
    @GetMapping("/{cropId}")
    public ResponseEntity<CropResponse> getCropById(
            @PathVariable Long cropId) {

        return ResponseEntity.ok(
                cropService.getCropById(cropId)
        );
    }

  
    @GetMapping
    public ResponseEntity<List<CropResponse>> getAllCrops() {

        return ResponseEntity.ok(
                cropService.getAllCrops()
        );
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<CropResponse>> getCropsByFarmer(
            @PathVariable Long farmerId) {

        return ResponseEntity.ok(
                cropService.getCropsByFarmer(farmerId)
        );
    }

    @PutMapping("/{cropId}")
    public ResponseEntity<CropResponse> updateCrop(
            @PathVariable Long cropId,
            @Valid @RequestBody CropRequest request) {

        return ResponseEntity.ok(
                cropService.updateCrop(cropId, request)
        );
    }

 
    @PatchMapping("/{cropId}/publish")
    public ResponseEntity<CropResponse> publishCrop(
            @PathVariable Long cropId) {

        return ResponseEntity.ok(
                cropService.publishCrop(cropId)
        );
    }

    @PatchMapping("/{cropId}/unpublish")
    public ResponseEntity<CropResponse> unpublishCrop(
            @PathVariable Long cropId) {

        return ResponseEntity.ok(
                cropService.unpublishCrop(cropId)
        );
    }

 
    @PatchMapping("/{cropId}/quantity")
    public ResponseEntity<CropResponse> updateQuantity(
            @PathVariable Long cropId,
            @RequestParam Double quantity) {

        return ResponseEntity.ok(
                cropService.updateQuantity(
                        cropId,
                        quantity
                )
        );
    }

    @DeleteMapping("/{cropId}")
    public ResponseEntity<Void> deleteCrop(
            @PathVariable Long cropId) {

        cropService.deleteCrop(cropId);

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/search")
    public ResponseEntity<List<CropResponse>> searchCrops(
            @RequestParam String cropName) {

        return ResponseEntity.ok(
                cropService.searchCrops(cropName)
        );
    }

 
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CropResponse>> getCropsByCategory(
            @PathVariable CropCategory category) {

        return ResponseEntity.ok(
                cropService.getCropsByCategory(category)
        );
    }


    @GetMapping("/city/{city}")
    public ResponseEntity<List<CropResponse>> getCropsByCity(
            @PathVariable String city) {

        return ResponseEntity.ok(
                cropService.getCropsByCity(city)
        );
    }
}