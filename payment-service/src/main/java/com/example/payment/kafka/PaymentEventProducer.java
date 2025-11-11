package com.example.payment.kafka;

import com.example.common.event.InventoryReservedEvent;
import com.example.common.event.PaymentCompletedEvent;
import com.example.common.event.PaymentFailedEvent;
import com.example.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Payment 이벤트 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "payment-events";

    /**
     * 결제 완료 이벤트 발행
     */
    public void publishPaymentCompleted(InventoryReservedEvent inventoryEvent, Payment payment) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(inventoryEvent.getOrderId())
                .paymentId(payment.getPaymentId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .completedAt(LocalDateTime.now())
                .build();

        log.info("📤 [Kafka Producer] 결제 완료 이벤트 발행 - orderId: {}, topic: {}",
                event.getOrderId(), TOPIC);

        CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future =
                kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ [Kafka Producer] 결제 완료 이벤트 발행 완료 - orderId: {}", event.getOrderId());
            } else {
                log.error("❌ [Kafka Producer] 이벤트 발행 실패 - orderId: {}", event.getOrderId(), ex);
            }
        });
    }

    /**
     * 결제 실패 이벤트 발행
     */
    public void publishPaymentFailed(InventoryReservedEvent inventoryEvent) {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .orderId(inventoryEvent.getOrderId())
                .productId(inventoryEvent.getProductId())
                .quantity(inventoryEvent.getQuantity())
                .reason("결제 실패: 잔액 부족")
                .failedAt(LocalDateTime.now())
                .build();

        log.info("📤 [Kafka Producer] 결제 실패 이벤트 발행 - orderId: {}, productId: {}, topic: {}",
                event.getOrderId(), event.getProductId(), TOPIC);

        CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future =
                kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ [Kafka Producer] 결제 실패 이벤트 발행 완료 - orderId: {}", event.getOrderId());
            } else {
                log.error("❌ [Kafka Producer] 이벤트 발행 실패 - orderId: {}", event.getOrderId(), ex);
            }
        });
    }
}
