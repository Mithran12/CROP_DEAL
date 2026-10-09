package com.cropdeal.order.repository;

import com.cropdeal.order.entity.OrderEventRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface OrderEventRecordRepository extends JpaRepository<OrderEventRecord, Long> {
	List<OrderEventRecord> findByAggregateTypeAndAggregateIdOrderByOccurredAtAsc(String type, String id);
}
