package com.example.payment.kafka;

import com.example.common.event.InventoryReservedEvent;
import com.example.payment.entity.Payment;
import com.example.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Inventory 이벤트 구독
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;

    /**
     * 재고 확보 성공 이벤트 수신 → 결제 처리
     */
    @KafkaListener(
            topics = "inventory-events",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("📩 [Kafka Consumer] 재고 확보 성공 이벤트 수신 - orderId: {}, 결제 처리 시작",
                event.getOrderId());

        try {
            Payment payment = paymentService.processPayment(
                    event.getOrderId(),
                    event.getTotalPrice()
            );

            if (payment != null) {
                // 결제 성공 → Order Service로 이벤트 발행
                paymentEventProducer.publishPaymentCompleted(event, payment);
            } else {
                // 결제 실패 → Inventory Service & Order Service로 실패 이벤트 발행
                paymentEventProducer.publishPaymentFailed(event);
            }
        } catch (Exception e) {
            log.error("❌ [Kafka Consumer] 결제 처리 실패 - orderId: {}", event.getOrderId(), e);
            paymentEventProducer.publishPaymentFailed(event);
        }
    }
}
