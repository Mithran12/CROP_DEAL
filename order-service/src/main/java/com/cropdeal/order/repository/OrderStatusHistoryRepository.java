package com.cropdeal.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cropdeal.order.entity.OrderStatusHistory;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderIdOrderByTimestampAsc(Long orderId);
}