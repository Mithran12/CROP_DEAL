package com.cropdeal.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cropdeal.order.entity.PurchaseRequest;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    Optional<PurchaseRequest> findByRequestNumber(String requestNumber);

    List<PurchaseRequest> findByFarmerId(Long farmerId);

    List<PurchaseRequest> findByDealerId(Long dealerId);
}