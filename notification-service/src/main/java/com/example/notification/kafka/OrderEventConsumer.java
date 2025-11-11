package com.example.notification.kafka;

import com.example.common.event.OrderCreatedEvent;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 주문 이벤트 Kafka Consumer
 * - order-events 토픽을 구독
 * - 주문 생성 이벤트 수신 시 NotificationService 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final NotificationService notificationService;

    /**
     * 주문 생성 이벤트 처리
     * @param event 주문 생성 이벤트
     */
    @KafkaListener(
            topics = "order-events",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("📩 [Kafka Consumer] 주문 이벤트 수신: orderId={}, userId={}, product={}",
                event.getOrderId(), event.getUserId(), event.getProductName());

        try {
            // 알림 발송
            notificationService.sendOrderNotification(event);
            log.info("✅ [Kafka Consumer] 알림 발송 완료: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ [Kafka Consumer] 알림 발송 실패: orderId={}, error={}",
                    event.getOrderId(), e.getMessage(), e);
            // Phase 3에서 DLQ 처리 추가 예정
            throw e; // Kafka Retry 트리거
        }
    }
}
