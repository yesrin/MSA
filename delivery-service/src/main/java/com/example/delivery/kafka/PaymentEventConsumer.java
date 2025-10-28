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
     *
     * 예외 처리 개선:
     * - try-catch 제거: 재시도 가능한 예외는 자동 재시도 (CommonErrorHandler)
     * - 재시도 후에도 실패 시 DLQ(Dead Letter Queue)로 이동
     */
    @KafkaListener(
            topics = "payment-events",
            groupId = "delivery-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("📩 [Kafka Consumer] 결제 완료 이벤트 수신 - orderId: {}, 배송 준비 시작",
                event.getOrderId());

        // 예외 발생 시 자동 재시도 (KafkaConsumerConfig의 ErrorHandler)
        Delivery delivery = deliveryService.prepareDelivery(event.getOrderId());

        // 이벤트 발행은 트랜잭션 커밋 후 (TransactionalEventListener 사용 권장)
        deliveryEventProducer.publishDeliveryPrepared(delivery);
    }
}
