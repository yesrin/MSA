package com.example.order.kafka;

import com.example.common.event.*;
import com.example.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 주문 이벤트 Kafka Producer
 * - 주문 생성/완료/취소 이벤트 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "order-events";

    /**
     * 주문 생성 이벤트 발행
     */
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .price(order.getPrice().intValue())
                .createdAt(LocalDateTime.now())
                .build();

        log.info("📤 [Kafka Producer] 주문 생성 이벤트 발행 - orderId: {}", order.getId());
        sendEvent(event);
    }

    /**
     * 주문 완료 이벤트 발행 (Saga 성공)
     */
    public void publishOrderCompleted(Order order) {
        OrderCompletedEvent event = OrderCompletedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .paymentId(order.getPaymentId())
                .completedAt(LocalDateTime.now())
                .build();

        log.info("📤 [Kafka Producer] 주문 완료 이벤트 발행 - orderId: {}", order.getId());
        sendEvent(event);
    }

    /**
     * 주문 취소 이벤트 발행 (Saga 실패)
     */
    public void publishOrderCancelled(Order order) {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .reason(order.getCancellationReason())
                .cancelledAt(LocalDateTime.now())
                .build();

        log.info("📤 [Kafka Producer] 주문 취소 이벤트 발행 - orderId: {}", order.getId());
        sendEvent(event);
    }

    /**
     * Kafka 이벤트 전송 공통 로직
     */
    private void sendEvent(Object event) {
        CompletableFuture<SendResult<String, Object>> future =
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
