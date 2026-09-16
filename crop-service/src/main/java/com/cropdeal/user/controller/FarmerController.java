package com.cropdeal.user.controller;

import com.cropdeal.user.dto.request.FarmerRequest;
import com.cropdeal.user.dto.response.FarmerResponse;
import com.cropdeal.user.service.FarmerService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/farmers")
public class FarmerController {

    private final FarmerService farmerService;

    public FarmerController(FarmerService farmerService) {
        this.farmerService = farmerService;
    }
    
    @PostMapping
    public ResponseEntity<FarmerResponse> createFarmer(@Valid @RequestBody FarmerRequest request) {

        FarmerResponse response = farmerService.createFarmer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{farmerId}")
    public ResponseEntity<FarmerResponse> getFarmerById(@PathVariable Long farmerId) {

        return ResponseEntity.ok(farmerService.getFarmerById(farmerId));
    }
    
    @GetMapping("/auth-user/{authUserId}")
    public ResponseEntity<FarmerResponse> getFarmerByAuthUserId(@PathVariable Long authUserId) {

        return ResponseEntity.ok(farmerService.getFarmerByAuthUserId(authUserId));
        
    }

    @PutMapping("/{farmerId}")
    public ResponseEntity<FarmerResponse> updateFarmer(@PathVariable Long farmerId,@Valid @RequestBody FarmerRequest request) {

        return ResponseEntity.ok(farmerService.updateFarmer(farmerId, request));
        
    }

    @PatchMapping("/{farmerId}/activate")
    public ResponseEntity<FarmerResponse> activateFarmer(@PathVariable Long farmerId) {

        return ResponseEntity.ok(farmerService.activateFarmer(farmerId));
   
    }

    @PatchMapping("/{farmerId}/deactivate")
    public ResponseEntity<FarmerResponse> deactivateFarmer(@PathVariable Long farmerId) {

        return ResponseEntity.ok(farmerService.deactivateFarmer(farmerId));
        
    }

    @DeleteMapping("/{farmerId}")
    public ResponseEntity<Void> deleteFarmer(@PathVariable Long farmerId) {
    	
        farmerService.deleteFarmer(farmerId);
        return ResponseEntity.noContent().build();
    }
}