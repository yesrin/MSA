package com.example.order.kafka;

import com.example.common.event.OrderCreatedEvent;
import com.example.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 주문 이벤트 Kafka Producer
 * - 주문 생성 시 order-events 토픽으로 이벤트 발행
 * - Phase 1: 단순 발행 (비동기)
 * - Phase 2: Outbox 패턴 적용 예정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private static final String TOPIC = "order-events";

    /**
     * 주문 생성 이벤트 발행
     * @param order 생성된 주문
     */
    public void publishOrderCreated(Order order) {
        BigDecimal totalPrice = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .totalPrice(totalPrice)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("📤 [Kafka Producer] 주문 이벤트 발행 시작 - orderId: {}, topic: {}",
                order.getId(), TOPIC);

        // 비동기 전송 (CompletableFuture)
        CompletableFuture<SendResult<String, OrderCreatedEvent>> future =
                kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);

        // 콜백으로 성공/실패 로깅
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ [Kafka Producer] 이벤트 발행 성공 - orderId: {}, partition: {}, offset: {}",
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("❌ [Kafka Producer] 이벤트 발행 실패 - orderId: {}, error: {}",
                        event.getOrderId(), ex.getMessage(), ex);
                // Phase 1: 실패 시 로그만 남김
                // Phase 2: Outbox 패턴으로 실패 처리
            }
        });
    }
}
