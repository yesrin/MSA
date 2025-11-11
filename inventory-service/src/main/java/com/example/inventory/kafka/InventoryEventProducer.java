package com.example.inventory.kafka;

import com.example.common.event.InventoryReservationFailedEvent;
import com.example.common.event.InventoryReservedEvent;
import com.example.common.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Inventory 이벤트 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "inventory-events";

    /**
     * 재고 확보 성공 이벤트 발행
     */
    public void publishInventoryReserved(OrderCreatedEvent orderEvent) {
        InventoryReservedEvent event = InventoryReservedEvent.builder()
                .orderId(orderEvent.getOrderId())
                .productId(orderEvent.getProductId())
                .productName(orderEvent.getProductName())
                .quantity(orderEvent.getQuantity())
                .totalPrice(orderEvent.getTotalPrice())
                .reservedAt(LocalDateTime.now())
                .build();

        log.info("📤 [Kafka Producer] 재고 확보 성공 이벤트 발행 - orderId: {}, productId: {}, topic: {}",
                event.getOrderId(), event.getProductId(), TOPIC);

        CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future =
                kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ [Kafka Producer] 재고 확보 성공 이벤트 발행 완료 - orderId: {}", event.getOrderId());
            } else {
                log.error("❌ [Kafka Producer] 이벤트 발행 실패 - orderId: {}", event.getOrderId(), ex);
            }
        });
    }

    /**
     * 재고 확보 실패 이벤트 발행
     */
    public void publishInventoryReservationFailed(Long orderId, Long productId,
                                                   Integer requestedQuantity, Integer availableQuantity) {
        InventoryReservationFailedEvent event = InventoryReservationFailedEvent.builder()
                .orderId(orderId)
                .productId(productId)
                .requestedQuantity(requestedQuantity)
                .availableQuantity(availableQuantity)
                .reason("재고 부족")
                .failedAt(LocalDateTime.now())
                .build();

        log.info("📤 [Kafka Producer] 재고 확보 실패 이벤트 발행 - orderId: {}, productId: {}, topic: {}",
                event.getOrderId(), event.getProductId(), TOPIC);

        CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future =
                kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ [Kafka Producer] 재고 확보 실패 이벤트 발행 완료 - orderId: {}", event.getOrderId());
            } else {
                log.error("❌ [Kafka Producer] 이벤트 발행 실패 - orderId: {}", event.getOrderId(), ex);
            }
        });
    }
}
