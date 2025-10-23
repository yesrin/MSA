package com.example.notification.service;

import com.example.common.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 알림 발송 서비스
 * - Saga Pattern의 각 단계별 알림 발송
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * 주문 생성 알림 (Saga 시작)
     */
    public void sendOrderCreatedNotification(OrderCreatedEvent event) {
        log.info("📧 ========== [주문 생성 알림] ==========");
        log.info("📧 주문이 접수되었습니다.");
        log.info("📧 주문 ID: {}", event.getOrderId());
        log.info("📧 상품명: {} ({}개)", event.getProductName(), event.getQuantity());
        log.info("📧 결제 금액: {}원", event.getPrice());
        log.info("📧 ========================================");
    }

    /**
     * 주문 완료 알림 (Saga 성공)
     */
    public void sendOrderCompletedNotification(OrderCompletedEvent event) {
        log.info("📧 ========== [주문 완료 알림] ==========");
        log.info("📧 ✅ 주문이 성공적으로 완료되었습니다!");
        log.info("📧 주문 ID: {}", event.getOrderId());
        log.info("📧 상품명: {} ({}개)", event.getProductName(), event.getQuantity());
        log.info("📧 결제 ID: {}", event.getPaymentId());
        log.info("📧 완료 시각: {}", event.getCompletedAt());
        log.info("📧 ========================================");
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
}
