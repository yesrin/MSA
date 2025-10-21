package com.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 주문 생성 이벤트
 * - Order Service에서 주문 생성 시 Kafka로 발행
 * - Notification Service 등 다른 서비스에서 구독
 * - 불변 객체 (Immutable): setter 없음, 생성 후 변경 불가
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {

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
     * 가격
     */
    private Integer price;

    /**
     * 이벤트 발생 시각
     */
    private LocalDateTime createdAt;
}
