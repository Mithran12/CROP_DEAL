package com.cropdeal.order.repository;

import com.cropdeal.order.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderSagaRepository extends JpaRepository<OrderSaga, Long> {
	Optional<OrderSaga> findByOrderId(Long orderId);
}
