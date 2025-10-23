package com.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 결제 실패 이벤트
 * - Payment Service에서 결제 실패 시 발행
 * - Inventory Service가 구독하여 재고 복구 (보상 트랜잭션)
 * - Order Service가 구독하여 주문 취소 처리
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 주문 ID
     */
    private Long orderId;

    /**
     * 상품명 (재고 복구용)
     */
    private String productName;

    /**
     * 수량 (재고 복구용)
     */
    private Integer quantity;

    /**
     * 실패 사유
     */
    private String reason;

    /**
     * 이벤트 발생 시각
     */
    private LocalDateTime failedAt;
}
