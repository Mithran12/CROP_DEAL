package com.cropdeal.report.messaging;
import com.cropdeal.report.client.OrderClient; import com.cropdeal.report.client.PaymentClient; import com.cropdeal.report.dto.*; import com.cropdeal.report.entity.GeneratedReceipt; import com.cropdeal.report.repository.GeneratedReceiptRepository; import org.springframework.amqp.rabbit.annotation.RabbitListener; import org.springframework.stereotype.Component; import java.time.LocalDateTime; import org.springframework.beans.factory.annotation.Value;
@Component public class ReceiptGenerationListener {
 private final GeneratedReceiptRepository repo; private final PaymentClient payments; private final OrderClient orders; @Value("${internal.api.key}") private String key;
 public ReceiptGenerationListener(GeneratedReceiptRepository r,PaymentClient p,OrderClient o){repo=r;payments=p;orders=o;}
 @RabbitListener(queues=RabbitConfig.QUEUE)
 public void onCompleted(OrderCompletedEvent event){
  if(repo.findByOrderId(event.getOrderId()).isPresent()) return;
  try{
   PaymentResponse p=payments.getPaymentByOrderInternal(event.getOrderId(),key); OrderResponse o=orders.getOrderInternal(event.getOrderId(),key);
   if(!"SUCCESS".equalsIgnoreCase(p.getStatus())) return;
   GeneratedReceipt r=new GeneratedReceipt();r.setOrderId(o.getId());r.setPaymentId(p.getId());r.setPaymentNumber(p.getPaymentNumber());r.setOrderNumber(o.getOrderNumber());r.setAmount(p.getAmount());r.setTransactionReference(p.getTransactionReference());r.setGeneratedAt(LocalDateTime.now());repo.save(r);
  }catch(RuntimeException ignored){ /* RabbitMQ will redeliver if configured by broker/container */ throw ignored; }
 }
}
