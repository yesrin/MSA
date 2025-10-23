package com.example.order.entity;

import com.example.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "orders",
        indexes = @Index(name = "idx_user_id", columnList = "userId"))
@Getter
@Setter
@NoArgsConstructor
public class Order extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * 결제 ID (Payment Service에서 받음)
     */
    @Column
    private String paymentId;

    /**
     * 배송 ID (Delivery Service에서 받음)
     */
    @Column
    private String deliveryId;

    /**
     * 취소 사유 (Saga 실패 시)
     */
    @Column
    private String cancellationReason;

    public Order(Long userId, String productName, Integer quantity, BigDecimal price) {
        this.userId = userId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.status = OrderStatus.PENDING;
    }

    /**
     * 재고 확보 완료
     */
    public void markInventoryReserved() {
        this.status = OrderStatus.INVENTORY_RESERVED;
    }

    /**
     * 결제 완료
     */
    public void markPaymentCompleted(String paymentId) {
        this.status = OrderStatus.PAYMENT_COMPLETED;
        this.paymentId = paymentId;
    }

    /**
     * 배송 시작
     */
    public void markDeliveryStarted(String deliveryId) {
        this.status = OrderStatus.DELIVERY_STARTED;
        this.deliveryId = deliveryId;
    }

    /**
     * 배송 완료
     */
    public void markDelivered() {
        this.status = OrderStatus.DELIVERED;
    }

    /**
     * 주문 완료 (최종)
     */
    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    /**
     * 주문 취소 (Saga 실패)
     */
    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = reason;
    }
}