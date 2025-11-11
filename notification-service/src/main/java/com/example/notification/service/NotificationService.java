package com.example.notification.service;

import com.example.common.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 알림 발송 서비스
 * - Phase 1: 로그 출력 (시뮬레이션)
 * - 향후 확장: 이메일/SMS 발송, Push 알림 등
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * 주문 생성 알림 발송
     * @param event 주문 생성 이벤트
     */
    public void sendOrderNotification(OrderCreatedEvent event) {
        log.info("📧 ========== 알림 발송 시작 ==========");
        log.info("📧 [알림] 주문이 생성되었습니다!");
        log.info("📧 주문 ID: {}", event.getOrderId());
        log.info("📧 사용자 ID: {}", event.getUserId());
        log.info("📧 상품명: {}", event.getProductName());
        log.info("📧 수량: {}", event.getQuantity());
        log.info("📧 가격: {}원", event.getPrice());
        log.info("📧 주문 시각: {}", event.getCreatedAt());
        log.info("📧 ========== 알림 발송 완료 ==========");

        // TODO: Phase 2+ 확장 사항
        // - 이메일 발송: emailService.send(...)
        // - SMS 발송: smsService.send(...)
        // - Push 알림: pushService.send(...)
        // - DB에 알림 이력 저장 (선택)
    }
}
