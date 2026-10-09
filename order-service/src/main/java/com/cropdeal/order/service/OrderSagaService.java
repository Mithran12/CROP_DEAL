package com.cropdeal.order.service;

import com.cropdeal.order.client.CropServiceClient;
import com.cropdeal.order.client.PaymentClient;
import com.cropdeal.order.dto.*;
import com.cropdeal.order.entity.*;
import com.cropdeal.order.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class OrderSagaService {
	private final OrderService orderService;
	private final OrderRepository orders;
	private final CropServiceClient crops;
	private final PaymentClient payments;
	private final OrderSagaRepository sagas;
	private final EventSourcingService events;

	public OrderSagaService(OrderService o, OrderRepository r, CropServiceClient c, PaymentClient p,
			OrderSagaRepository s, EventSourcingService e) {
		orderService = o;
		orders = r;
		crops = c;
		payments = p;
		sagas = s;
		events = e;
	}

	public SagaResponse start(Long negotiationId, SagaPaymentRequest payment) {
		OrderResponse created = orderService.createOrderFromNegotiation(negotiationId);
		Order order = orders.findById(created.getId()).orElseThrow();
		
		order.setStatus(OrderStatus.ACCEPTED);
		order.setCropReserved(false);
		orders.save(order);
		OrderSaga saga = new OrderSaga();
		saga.setOrderId(order.getId());
		saga.setCropId(order.getCropId());
		saga.setQuantity(order.getQuantity());
		saga.setStatus(SagaStatus.STARTED);
		saga.setStartedAt(LocalDateTime.now());
		saga.setUpdatedAt(LocalDateTime.now());
		saga = sagas.save(saga);
		try {
			crops.reduceQuantity(order.getCropId(), order.getQuantity());
			order.setCropReserved(true);
			orders.save(order);
			saga.setStatus(SagaStatus.CROP_RESERVED);
			saga.setUpdatedAt(LocalDateTime.now());
			sagas.save(saga);
			events.append("ORDER", String.valueOf(order.getId()), "CROP_RESERVED",
					java.util.Map.of("quantity", order.getQuantity()));
			payment.setOrderId(order.getId());
			saga.setStatus(SagaStatus.PAYMENT_INITIATED);
			saga.setUpdatedAt(LocalDateTime.now());
			sagas.save(saga);
			PaymentResponse p = payments.create(payment);
			if (p == null || !"SUCCESS".equalsIgnoreCase(p.getStatus()))
				throw new IllegalStateException("Payment did not complete successfully");
			saga.setStatus(SagaStatus.PAYMENT_SUCCESS);
			saga.setUpdatedAt(LocalDateTime.now());
			sagas.save(saga);
			events.append("ORDER", String.valueOf(order.getId()), "PAYMENT_SUCCESS",
					java.util.Map.of("sagaId", saga.getId() == null ? 0L : saga.getId()));
			SagaResponse r = new SagaResponse();
			r.setOrderId(order.getId());
			r.setStatus("PAYMENT_SUCCESS");
			r.setMessage("Saga completed payment successfully; delivery QR is ready");
			return r;
		} catch (RuntimeException ex) {
			try {
				if (Boolean.TRUE.equals(order.getCropReserved()))
					crops.restoreQuantity(order.getCropId(), order.getQuantity());
			} catch (RuntimeException ignored) {
			}
			try {
				order.setCropReserved(false);
				if (order.getStatus() != OrderStatus.COMPLETED)
					order.setStatus(OrderStatus.CANCELLED);
				orders.save(order);
			} catch (RuntimeException ignored) {
			}
			saga.setStatus(SagaStatus.COMPENSATED);
			saga.setFailureReason(ex.getMessage());
			saga.setUpdatedAt(LocalDateTime.now());
			sagas.save(saga);
			events.append("ORDER", String.valueOf(order.getId()), "SAGA_COMPENSATED",
					java.util.Map.of("reason", String.valueOf(ex.getMessage())));
			SagaResponse r = new SagaResponse();
			r.setOrderId(order.getId());
			r.setStatus("COMPENSATED");
			r.setMessage("Payment failed; crop reservation was released and order cancelled");
			return r;
		}
	}
}
