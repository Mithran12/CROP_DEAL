package com.cropdeal.user.controller;

import com.cropdeal.user.dto.request.DealerRequest;
import com.cropdeal.user.dto.response.DealerResponse;
import com.cropdeal.user.service.DealerService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/dealers")
public class DealerController {

    private final DealerService dealerService;

    public DealerController(DealerService dealerService) {
        this.dealerService = dealerService;
    }


    @PostMapping
    public ResponseEntity<DealerResponse> createDealer(@Valid @RequestBody DealerRequest request) {

        DealerResponse response = dealerService.createDealer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{dealerId}")
    public ResponseEntity<DealerResponse> getDealerById(@PathVariable Long dealerId) {

        return ResponseEntity.ok(dealerService.getDealerById(dealerId));
       
    }

    @GetMapping("/auth-user/{authUserId}")
    public ResponseEntity<DealerResponse> getDealerByAuthUserId(@PathVariable Long authUserId) {

        return ResponseEntity.ok(dealerService.getDealerByAuthUserId(authUserId));
 
    }

    @PutMapping("/{dealerId}")
    public ResponseEntity<DealerResponse> updateDealer(@PathVariable Long dealerId, @Valid @RequestBody DealerRequest request) {

        return ResponseEntity.ok(dealerService.updateDealer(dealerId, request));
        
    }

    @PatchMapping("/{dealerId}/activate")
    public ResponseEntity<DealerResponse> activateDealer(@PathVariable Long dealerId) {

        return ResponseEntity.ok(dealerService.activateDealer(dealerId));
      
    }

    @PatchMapping("/{dealerId}/deactivate")
    public ResponseEntity<DealerResponse> deactivateDealer(@PathVariable Long dealerId) {

        return ResponseEntity.ok(dealerService.deactivateDealer(dealerId));
      
    }

    @DeleteMapping("/{dealerId}")
    public ResponseEntity<Void> deleteDealer(@PathVariable Long dealerId) {

        dealerService.deleteDealer(dealerId);

        return ResponseEntity.noContent().build();
    }
}