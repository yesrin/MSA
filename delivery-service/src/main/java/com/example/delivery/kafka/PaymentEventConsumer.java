package com.example.delivery.kafka;

import com.example.common.event.PaymentCompletedEvent;
import com.example.delivery.entity.Delivery;
import com.example.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Payment 이벤트 구독
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final DeliveryService deliveryService;
    private final DeliveryEventProducer deliveryEventProducer;

    /**
     * 결제 완료 이벤트 수신 → 배송 준비 시작
     */
    @KafkaListener(
            topics = "payment-events",
            groupId = "delivery-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("📩 [Kafka Consumer] 결제 완료 이벤트 수신 - orderId: {}, 배송 준비 시작",
                event.getOrderId());

        try {
            Delivery delivery = deliveryService.prepareDelivery(event.getOrderId());

            // 배송 시작 이벤트 발행 (즉시 발행, 실제 배송은 비동기)
            deliveryEventProducer.publishDeliveryStarted(delivery);

        } catch (Exception e) {
            log.error("❌ [Kafka Consumer] 배송 준비 실패 - orderId: {}", event.getOrderId(), e);
        }
    }
}
