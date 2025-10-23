package com.example.notification.service;

import com.example.common.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 알림 발송 서비스
 * - E-Commerce Saga Pattern의 각 단계별 알림 발송
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * 주문 생성 알림
     */
    public void sendOrderCreatedNotification(OrderCreatedEvent event) {
        log.info("📧 ========== [주문 접수 알림] ==========");
        log.info("📧 주문이 접수되었습니다.");
        log.info("📧 주문 ID: {}", event.getOrderId());
        log.info("📧 상품명: {} ({}개)", event.getProductName(), event.getQuantity());
        log.info("📧 결제 금액: {}원", event.getPrice());
        log.info("📧 ========================================");
    }

    /**
     * 배송 시작 알림
     */
    public void sendDeliveryStartedNotification(DeliveryStartedEvent event) {
        log.info("📧 ========== [배송 시작 알림] ==========");
        log.info("📧 🚚 상품이 배송 시작되었습니다!");
        log.info("📧 주문 ID: {}", event.getOrderId());
        log.info("📧 배송 번호: {}", event.getDeliveryId());
        log.info("📧 택배사: {}", event.getCarrier());
        log.info("📧 배송지: {}", event.getAddress());
        log.info("📧 ========================================");
    }

    /**
     * 배송 완료 알림
     */
    public void sendDeliveryCompletedNotification(DeliveryCompletedEvent event) {
        log.info("📧 ========== [배송 완료 알림] ==========");
        log.info("📧 📦 상품이 배송 완료되었습니다!");
        log.info("📧 주문 ID: {}", event.getOrderId());
        log.info("📧 배송 번호: {}", event.getDeliveryId());
        log.info("📧 완료 시각: {}", event.getCompletedAt());
        log.info("📧 ========================================");
    }

    /**
     * 주문 완료 알림 (최종)
     */
    public void sendOrderCompletedNotification(OrderCompletedEvent event) {
        log.info("📧 ========== [주문 최종 완료 알림] ==========");
        log.info("📧 ✅ 모든 처리가 완료되었습니다!");
        log.info("📧 주문 ID: {}", event.getOrderId());
        log.info("📧 상품명: {} ({}개)", event.getProductName(), event.getQuantity());
        log.info("📧 결제 ID: {}", event.getPaymentId());
        log.info("📧 감사합니다!");
        log.info("📧 ==========================================");
    }

    /**
     * 주문 취소 알림 (Saga 실패)
     */
    public void sendOrderCancelledNotification(OrderCancelledEvent event) {
        log.info("📧 ========== [주문 취소 알림] ==========");
        log.info("📧 ❌ 주문이 취소되었습니다.");
        log.info("📧 주문 ID: {}", event.getOrderId());
        log.info("📧 취소 사유: {}", event.getReason());
        log.info("📧 취소 시각: {}", event.getCancelledAt());
        log.info("📧 ========================================");
    }

    /**
     * 배송 실패 알림
     */
    public void sendDeliveryFailedNotification(DeliveryFailedEvent event) {
        log.info("📧 ========== [배송 실패 알림] ==========");
        log.info("📧 ⚠️ 배송에 실패했습니다.");
        log.info("📧 주문 ID: {}", event.getOrderId());
        log.info("📧 배송 번호: {}", event.getDeliveryId());
        log.info("📧 실패 사유: {}", event.getReason());
        log.info("📧 고객센터로 문의 부탁드립니다.");
        log.info("📧 ========================================");
    }
}
