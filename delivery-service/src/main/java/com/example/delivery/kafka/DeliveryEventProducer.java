package com.example.delivery.kafka;

import com.example.common.event.DeliveryCompletedEvent;
import com.example.common.event.DeliveryFailedEvent;
import com.example.common.event.DeliveryStartedEvent;
import com.example.delivery.entity.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Delivery 이벤트 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "delivery-events";

    /**
     * 배송 시작 이벤트 발행
     */
    public void publishDeliveryStarted(Delivery delivery) {
        DeliveryStartedEvent event = DeliveryStartedEvent.builder()
                .orderId(delivery.getOrderId())
                .deliveryId(delivery.getDeliveryId())
                .address(delivery.getAddress())
                .carrier(delivery.getCarrier())
                .startedAt(LocalDateTime.now())
                .build();

        log.info("📤 [Kafka Producer] 배송 시작 이벤트 발행 - orderId: {}", delivery.getOrderId());
        sendEvent(event);
    }

    /**
     * 배송 완료 이벤트 발행
     */
    public void publishDeliveryCompleted(Delivery delivery) {
        DeliveryCompletedEvent event = DeliveryCompletedEvent.builder()
                .orderId(delivery.getOrderId())
                .deliveryId(delivery.getDeliveryId())
                .completedAt(LocalDateTime.now())
                .build();

        log.info("📤 [Kafka Producer] 배송 완료 이벤트 발행 - orderId: {}", delivery.getOrderId());
        sendEvent(event);
    }

    /**
     * 배송 실패 이벤트 발행
     */
    public void publishDeliveryFailed(Delivery delivery) {
        DeliveryFailedEvent event = DeliveryFailedEvent.builder()
                .orderId(delivery.getOrderId())
                .deliveryId(delivery.getDeliveryId())
                .reason(delivery.getFailureReason())
                .failedAt(LocalDateTime.now())
                .build();

        log.info("📤 [Kafka Producer] 배송 실패 이벤트 발행 - orderId: {}", delivery.getOrderId());
        sendEvent(event);
    }

    /**
     * Kafka 이벤트 전송 공통 로직
     */
    private void sendEvent(Object event) {
        CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future =
                kafkaTemplate.send(TOPIC, event.toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ [Kafka Producer] 이벤트 발행 성공");
            } else {
                log.error("❌ [Kafka Producer] 이벤트 발행 실패", ex);
            }
        });
    }
}
