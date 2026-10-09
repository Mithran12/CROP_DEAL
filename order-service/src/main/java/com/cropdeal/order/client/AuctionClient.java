package com.cropdeal.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cropdeal.order.dto.AuctionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "AUCTION-SERVICE")
public interface AuctionClient {

    @GetMapping("/auctions/{id}")
    AuctionResponse getAuction(@PathVariable("id") Long id);

    @PatchMapping("/auctions/{id}/complete")
    Object completeAuction(@PathVariable("id") Long id);

    @PatchMapping("/auctions/{id}/link-order")
    Object linkOrder(@PathVariable("id") Long id,
                     @RequestParam("orderId") Long orderId,
                     @RequestParam(value = "orderNumber", required = false) String orderNumber);
}
