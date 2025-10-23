package com.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 배송 실패 이벤트
 * - Delivery Service에서 배송 실패 시 발행 (수령 거부, 주소 오류 등)
 * - Order Service가 구독하여 고객센터 처리
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryFailedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 주문 ID
     */
    private Long orderId;

    /**
     * 배송 ID
     */
    private String deliveryId;

    /**
     * 실패 사유
     */
    private String reason;

    /**
     * 이벤트 발생 시각
     */
    private LocalDateTime failedAt;
}
