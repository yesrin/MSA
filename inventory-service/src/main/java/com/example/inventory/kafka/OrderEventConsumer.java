package com.example.inventory.kafka;

import com.example.common.event.OrderCreatedEvent;
import com.example.common.event.PaymentFailedEvent;
import com.example.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Order/Payment 이벤트 구독
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;

    /**
     * 주문 생성 이벤트 수신 → 재고 확보 시도
     */
    @KafkaListener(
            topics = "order-events",
            groupId = "inventory-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("📩 [Kafka Consumer] 주문 생성 이벤트 수신 - orderId: {}, product: {}, quantity: {}",
                event.getOrderId(), event.getProductName(), event.getQuantity());

        try {
            boolean success = inventoryService.reserveInventory(
                    event.getProductName(),
                    event.getQuantity()
            );

            if (success) {
                // 재고 확보 성공 → Payment Service로 이벤트 발행
                inventoryEventProducer.publishInventoryReserved(event);
            } else {
                // 재고 부족 → Order Service로 실패 이벤트 발행
                int availableQuantity = inventoryService.getAvailableQuantity(event.getProductName());
                inventoryEventProducer.publishInventoryReservationFailed(
                        event.getOrderId(),
                        event.getProductName(),
                        event.getQuantity(),
                        availableQuantity
                );
            }
        } catch (Exception e) {
            log.error("❌ [Kafka Consumer] 재고 처리 실패 - orderId: {}", event.getOrderId(), e);
            inventoryEventProducer.publishInventoryReservationFailed(
                    event.getOrderId(),
                    event.getProductName(),
                    event.getQuantity(),
                    0
            );
        }
    }

    /**
     * 결제 실패 이벤트 수신 → 재고 복구 (보상 트랜잭션)
     */
    @KafkaListener(
            topics = "payment-events",
            groupId = "inventory-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("📩 [Kafka Consumer] 결제 실패 이벤트 수신 - orderId: {}, 재고 복구 시작",
                event.getOrderId());

        try {
            inventoryService.releaseInventory(
                    event.getProductName(),
                    event.getQuantity()
            );
            log.info("✅ [보상 트랜잭션] 재고 복구 완료 - orderId: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ [보상 트랜잭션] 재고 복구 실패 - orderId: {}", event.getOrderId(), e);
            // 실무에서는 DLQ(Dead Letter Queue)로 전송하거나 알림 발송
        }
    }
}
