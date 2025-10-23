package com.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 주문 완료 이벤트
 * - Order Service에서 전체 Saga가 성공적으로 완료되었을 때 발행
 * - Notification Service 등에서 구독하여 완료 알림 발송
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent implements Serializable {

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
     * 상품명
     */
    private String productName;

    /**
     * 수량
     */
    private Integer quantity;

    /**
     * 결제 ID
     */
    private String paymentId;

    /**
     * 이벤트 발생 시각
     */
    private LocalDateTime completedAt;
}
