package com.example.order.kafka;

import com.example.common.event.*;
import com.example.order.entity.Order;
import com.example.order.exception.OrderNotFoundException;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saga 이벤트 구독 (Inventory, Payment, Delivery 이벤트)
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
                    .orElseThrow(() -> new OrderNotFoundException(failedEvent.getOrderId()));

            order.cancel(failedEvent.getReason());
            orderRepository.save(order);

            log.info("❌ [Saga Failed] 주문 취소 완료 - orderId: {}, reason: {}",
                    failedEvent.getOrderId(), failedEvent.getReason());

            // 주문 취소 이벤트 발행 (Notification Service로)
            orderEventProducer.publishOrderCancelled(order);
        }
    }

    /**
     * 결제 완료/실패 이벤트 수신
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
                    .orElseThrow(() -> new OrderNotFoundException(completedEvent.getOrderId()));

            order.markPaymentCompleted(completedEvent.getPaymentId());
            orderRepository.save(order);

            log.info("✅ [결제 완료] orderId: {}, 다음: 배송 시작 대기", completedEvent.getOrderId());

        } else if (event instanceof PaymentFailedEvent failedEvent) {
            log.info("📩 [Kafka Consumer] 결제 실패 이벤트 수신 - orderId: {}",
                    failedEvent.getOrderId());

            Order order = orderRepository.findById(failedEvent.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException(failedEvent.getOrderId()));

            order.cancel(failedEvent.getReason());
            orderRepository.save(order);

            log.info("❌ [Saga Failed] 주문 취소 완료 - orderId: {}, reason: {}",
                    failedEvent.getOrderId(), failedEvent.getReason());

            // 주문 취소 이벤트 발행 (Notification Service로)
            orderEventProducer.publishOrderCancelled(order);
        }
    }

    /**
     * 배송 시작/완료/실패 이벤트 수신
     */
    @KafkaListener(
            topics = "delivery-events",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleDeliveryEvent(Object event) {
        if (event instanceof DeliveryStartedEvent startedEvent) {
            log.info("📩 [Kafka Consumer] 배송 시작 이벤트 수신 - orderId: {}, deliveryId: {}",
                    startedEvent.getOrderId(), startedEvent.getDeliveryId());

            Order order = orderRepository.findById(startedEvent.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다"));

            order.markDeliveryStarted(startedEvent.getDeliveryId());
            orderRepository.save(order);

            log.info("🚚 [배송 시작] orderId: {}, deliveryId: {}",
                    startedEvent.getOrderId(), startedEvent.getDeliveryId());

        } else if (event instanceof DeliveryCompletedEvent completedEvent) {
            log.info("📩 [Kafka Consumer] 배송 완료 이벤트 수신 - orderId: {}",
                    completedEvent.getOrderId());

            Order order = orderRepository.findById(completedEvent.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다"));

            order.markDelivered();
            order.complete(); // 최종 완료
            orderRepository.save(order);

            log.info("✅ [Saga Success] 주문 최종 완료 - orderId: {}", completedEvent.getOrderId());

            // 주문 완료 이벤트 발행 (Notification Service로)
            orderEventProducer.publishOrderCompleted(order);

        } else if (event instanceof DeliveryFailedEvent failedEvent) {
            log.info("📩 [Kafka Consumer] 배송 실패 이벤트 수신 - orderId: {}",
                    failedEvent.getOrderId());

            // 배송 실패는 고객센터 처리 (주문은 유지)
            log.warn("⚠️ [배송 실패] orderId: {}, reason: {} - 고객센터 처리 필요",
                    failedEvent.getOrderId(), failedEvent.getReason());
        }
    }
}
