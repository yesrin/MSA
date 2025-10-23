package com.example.order.entity;

/**
 * E-Commerce 주문 상태 (Saga Pattern)
 */
public enum OrderStatus {
    PENDING,              // 주문 생성 (Saga 시작)
    INVENTORY_RESERVED,   // 재고 확보 완료
    PAYMENT_COMPLETED,    // 결제 완료
    DELIVERY_STARTED,     // 배송 시작
    DELIVERED,            // 배송 완료
    COMPLETED,            // 주문 완료 (최종)
    CANCELLED             // 주문 취소 (Saga 실패)
}
