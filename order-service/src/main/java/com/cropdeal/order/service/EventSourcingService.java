package com.cropdeal.order.service;

import com.cropdeal.order.entity.OrderEventRecord;
import com.cropdeal.order.repository.OrderEventRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventSourcingService {
	private final OrderEventRecordRepository repo;
	private final ObjectMapper mapper;

	public EventSourcingService(OrderEventRecordRepository r, ObjectMapper m) {
		repo = r;
		mapper = m;
	}

	public void append(String type, String id, String event, Object payload) {
		try {
			OrderEventRecord r = new OrderEventRecord();
			r.setAggregateType(type);
			r.setAggregateId(id);
			r.setEventType(event);
			r.setPayload(mapper.writeValueAsString(payload));
			r.setOccurredAt(LocalDateTime.now());
			repo.save(r);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to append domain event", e);
		}
	}

	public List<OrderEventRecord> history(String type, String id) {
		return repo.findByAggregateTypeAndAggregateIdOrderByOccurredAtAsc(type, id);
	}
}
