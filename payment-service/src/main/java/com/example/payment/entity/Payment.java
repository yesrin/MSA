package com.example.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 엔티티
 */
@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 주문 ID (연관관계)
     */
    @Column(nullable = false)
    private Long orderId;

    /**
     * 결제 ID (외부 PG사 트랜잭션 ID)
     */
    @Column(nullable = false, unique = true)
    private String paymentId;

    /**
     * 결제 금액
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * 결제 수단
     */
    @Column(nullable = false)
    private String paymentMethod;

    /**
     * 결제 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /**
     * 결제 시각
     */
    @Column(nullable = false)
    private LocalDateTime paymentAt;

    public Payment(Long orderId, String paymentId, BigDecimal amount, String paymentMethod) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.COMPLETED;
        this.paymentAt = LocalDateTime.now();
    }

    public enum PaymentStatus {
        COMPLETED,  // 결제 완료
        CANCELLED   // 결제 취소 (보상 트랜잭션)
    }

    /**
     * 결제 취소 (보상 트랜잭션)
     */
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }
}
