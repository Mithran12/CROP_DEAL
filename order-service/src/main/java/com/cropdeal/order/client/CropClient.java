package com.cropdeal.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.cropdeal.order.dto.CropResponse;

@FeignClient(name = "CROP-SERVICE")
public interface CropClient {

    @GetMapping("/crops/{id}")
    CropResponse getCropById(@PathVariable Long id);

    @PatchMapping("/crops/{id}/restore-quantity")
    CropResponse restoreQuantity(@PathVariable Long id,@RequestParam Double quantity,@RequestHeader("X-Internal-Key") String internalKey);

    @PatchMapping("/crops/{id}/reduce-quantity")
    CropResponse reduceQuantity(
            @PathVariable Long id,
            @RequestParam Double quantity,
            @RequestHeader("X-Internal-Key") String internalKey
    );
}
