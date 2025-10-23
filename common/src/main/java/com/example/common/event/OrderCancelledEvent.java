package com.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 주문 취소 이벤트
 * - Order Service에서 Saga 실패 시 발행
 * - Notification Service 등에서 구독하여 취소 알림 발송
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 주문 ID
     */
    private Long orderId;

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 취소 사유
     */
    private String reason;

    /**
     * 이벤트 발생 시각
     */
    private LocalDateTime cancelledAt;
}
