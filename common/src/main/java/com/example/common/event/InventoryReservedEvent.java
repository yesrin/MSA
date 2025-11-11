package com.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고 확보 성공 이벤트
 * - Inventory Service에서 재고 차감 성공 시 발행
 * - Payment Service가 구독하여 결제 처리 시작
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent implements Serializable {

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
     * 상품명 (스냅샷)
     */
    private String productName;

    /**
     * 확보된 수량
     */
    private Integer quantity;

    /**
     * 결제할 금액
     */
    private BigDecimal totalPrice;

    /**
     * 이벤트 발생 시각
     */
    private LocalDateTime reservedAt;
}
