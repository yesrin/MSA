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
     *
     * 개선사항:
     * - DB/Redis 연결 실패 시 자동 재시도 (ErrorHandler)
     * - 비즈니스 실패(재고 부족)는 명시적 처리
     */
    @KafkaListener(
            topics = "order-events",
            groupId = "inventory-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("📩 [Kafka Consumer] 주문 생성 이벤트 수신 - orderId: {}, productId: {}, quantity: {}",
                event.getOrderId(), event.getProductId(), event.getQuantity());

        // 분산 락 획득 실패나 DB 연결 실패 시 자동 재시도
        boolean success = inventoryService.reserveInventory(
                event.getProductId(),
                event.getQuantity()
        );

        if (success) {
            // 재고 확보 성공 → 트랜잭션 커밋 후 이벤트 발행
            inventoryEventProducer.publishInventoryReserved(event);
        } else {
            // 재고 부족 (비즈니스 실패) → 실패 이벤트 발행
            int availableQuantity = inventoryService.getAvailableQuantity(event.getProductId());
            inventoryEventProducer.publishInventoryReservationFailed(
                    event.getOrderId(),
                    event.getProductId(),
                    event.getQuantity(),
                    availableQuantity
            );
        }
    }

    /**
     * 결제 실패 이벤트 수신 → 재고 복구 (보상 트랜잭션)
     *
     * 보상 트랜잭션은 반드시 성공해야 하므로 자동 재시도 적용
     */
    @KafkaListener(
            topics = "payment-events",
            groupId = "inventory-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("📩 [Kafka Consumer] 결제 실패 이벤트 수신 - orderId: {}, 재고 복구 시작",
                event.getOrderId());

        // 재고 복구 실패 시 자동 재시도 (보상 트랜잭션은 반드시 성공해야 함)
        inventoryService.releaseInventory(
                event.getProductId(),
                event.getQuantity()
        );

        log.info("✅ [보상 트랜잭션] 재고 복구 완료 - orderId: {}", event.getOrderId());
    }
}
