package com.example.notification.kafka;

import com.example.common.event.*;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 주문 이벤트 Kafka Consumer
 * - order-events 토픽을 구독
 * - 주문 생성/완료/취소 이벤트 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final NotificationService notificationService;

    /**
     * 주문 이벤트 처리 (생성/완료/취소)
     */
    @KafkaListener(
            topics = "order-events",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderEvent(Object event) {
        try {
            if (event instanceof OrderCreatedEvent createdEvent) {
                log.info("📩 [Kafka Consumer] 주문 생성 이벤트 수신 - orderId: {}, userId: {}",
                        createdEvent.getOrderId(), createdEvent.getUserId());
                notificationService.sendOrderCreatedNotification(createdEvent);

            } else if (event instanceof OrderCompletedEvent completedEvent) {
                log.info("📩 [Kafka Consumer] 주문 완료 이벤트 수신 - orderId: {}, paymentId: {}",
                        completedEvent.getOrderId(), completedEvent.getPaymentId());
                notificationService.sendOrderCompletedNotification(completedEvent);

            } else if (event instanceof OrderCancelledEvent cancelledEvent) {
                log.info("📩 [Kafka Consumer] 주문 취소 이벤트 수신 - orderId: {}, reason: {}",
                        cancelledEvent.getOrderId(), cancelledEvent.getReason());
                notificationService.sendOrderCancelledNotification(cancelledEvent);
            }

            log.info("✅ [Kafka Consumer] 알림 발송 완료");
        } catch (Exception e) {
            log.error("❌ [Kafka Consumer] 알림 발송 실패", e);
            // Phase 3: DLQ 처리 추가 예정
        }
    }
}
