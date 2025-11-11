package com.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 배송 시작 이벤트
 * - Delivery Service에서 배송 시작 시 발행
 * - Order Service가 구독하여 주문 상태 업데이트
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStartedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 주문 ID
     */
    private Long orderId;

    /**
     * 배송 ID (추적 번호)
     */
    private String deliveryId;

    /**
     * 배송지 주소
     */
    private String address;

    /**
     * 택배사
     */
    private String carrier;

    /**
     * 이벤트 발생 시각
     */
    private LocalDateTime startedAt;
}
