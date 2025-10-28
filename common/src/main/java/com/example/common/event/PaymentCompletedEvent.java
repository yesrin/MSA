package com.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 결제 완료 이벤트
 * - Payment Service에서 결제 성공 시 발행
 * - Order Service가 구독하여 주문 완료 처리
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 주문 ID
     */
    private Long orderId;

    /**
     * 결제 ID (외부 PG사 트랜잭션 ID)
     */
    private String paymentId;

    /**
     * 결제 금액
     */
    private Integer amount;

    /**
     * 결제 수단 (CARD, BANK_TRANSFER 등)
     */
    private String paymentMethod;

    /**
     * 이벤트 발생 시각
     */
    private LocalDateTime completedAt;
}
