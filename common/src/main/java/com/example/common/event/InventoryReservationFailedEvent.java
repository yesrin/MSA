package com.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 재고 확보 실패 이벤트
 * - Inventory Service에서 재고 부족 시 발행
 * - Order Service가 구독하여 주문 취소 처리
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationFailedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 주문 ID
     */
    private Long orderId;

    /**
     * 상품 ID
     */
    private Long productId;

    /**
     * 요청 수량
     */
    private Integer requestedQuantity;

    /**
     * 현재 재고 수량
     */
    private Integer availableQuantity;

    /**
     * 실패 사유
     */
    private String reason;

    /**
     * 이벤트 발생 시각
     */
    private LocalDateTime failedAt;
}
