package com.example.order.kafka;

import com.example.common.event.*;
import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga 이벤트 구독 (Inventory, Payment 이벤트)
 * Order Service는 Saga Orchestrator 역할
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventConsumer {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    /**
     * 재고 확보 실패 이벤트 수신 → 주문 취소
     */
    @KafkaListener(
            topics = "inventory-events",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryEvent(Object event) {
        if (event instanceof InventoryReservationFailedEvent failedEvent) {
            log.info("📩 [Kafka Consumer] 재고 확보 실패 이벤트 수신 - orderId: {}",
                    failedEvent.getOrderId());

            Order order = orderRepository.findById(failedEvent.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

            order.cancel(failedEvent.getReason());
            orderRepository.save(order);

            log.info("❌ [Saga Failed] 주문 취소 완료 - orderId: {}, reason: {}",
                    failedEvent.getOrderId(), failedEvent.getReason());

            // 주문 취소 이벤트 발행 (Notification Service로)
            orderEventProducer.publishOrderCancelled(order);
        }
    }

    /**
     * 결제 완료 이벤트 수신 → 주문 완료
     * 결제 실패 이벤트 수신 → 주문 취소
     */
    @KafkaListener(
            topics = "payment-events",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentEvent(Object event) {
        if (event instanceof PaymentCompletedEvent completedEvent) {
            log.info("📩 [Kafka Consumer] 결제 완료 이벤트 수신 - orderId: {}, paymentId: {}",
                    completedEvent.getOrderId(), completedEvent.getPaymentId());

            Order order = orderRepository.findById(completedEvent.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

            order.markPaymentCompleted(completedEvent.getPaymentId());
            order.complete();
            orderRepository.save(order);

            log.info("✅ [Saga Success] 주문 완료 - orderId: {}, paymentId: {}",
                    completedEvent.getOrderId(), completedEvent.getPaymentId());

            // 주문 완료 이벤트 발행 (Notification Service로)
            orderEventProducer.publishOrderCompleted(order);

        } else if (event instanceof PaymentFailedEvent failedEvent) {
            log.info("📩 [Kafka Consumer] 결제 실패 이벤트 수신 - orderId: {}",
                    failedEvent.getOrderId());

            Order order = orderRepository.findById(failedEvent.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

            order.cancel(failedEvent.getReason());
            orderRepository.save(order);

            log.info("❌ [Saga Failed] 주문 취소 완료 - orderId: {}, reason: {}",
                    failedEvent.getOrderId(), failedEvent.getReason());

            // 주문 취소 이벤트 발행 (Notification Service로)
            orderEventProducer.publishOrderCancelled(order);
        }
    }
}
