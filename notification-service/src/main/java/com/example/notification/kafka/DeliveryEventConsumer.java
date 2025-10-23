package com.example.notification.kafka;

import com.example.common.event.*;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 배송 이벤트 Kafka Consumer
 * - delivery-events 토픽을 구독
 * - 배송 시작/완료/실패 이벤트 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventConsumer {

    private final NotificationService notificationService;

    /**
     * 배송 이벤트 처리 (시작/완료/실패)
     */
    @KafkaListener(
            topics = "delivery-events",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryEvent(Object event) {
        try {
            if (event instanceof DeliveryStartedEvent startedEvent) {
                log.info("📩 [Kafka Consumer] 배송 시작 이벤트 수신 - orderId: {}, deliveryId: {}",
                        startedEvent.getOrderId(), startedEvent.getDeliveryId());
                notificationService.sendDeliveryStartedNotification(startedEvent);

            } else if (event instanceof DeliveryCompletedEvent completedEvent) {
                log.info("📩 [Kafka Consumer] 배송 완료 이벤트 수신 - orderId: {}",
                        completedEvent.getOrderId());
                notificationService.sendDeliveryCompletedNotification(completedEvent);

            } else if (event instanceof DeliveryFailedEvent failedEvent) {
                log.info("📩 [Kafka Consumer] 배송 실패 이벤트 수신 - orderId: {}",
                        failedEvent.getOrderId());
                notificationService.sendDeliveryFailedNotification(failedEvent);
            }

            log.info("✅ [Kafka Consumer] 알림 발송 완료");
        } catch (Exception e) {
            log.error("❌ [Kafka Consumer] 알림 발송 실패", e);
        }
    }
}
