package com.example.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 배송 엔티티
 */
@Entity
@Table(name = "delivery")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 주문 ID (연관관계)
     */
    @Column(nullable = false)
    private Long orderId;

    /**
     * 배송 ID (추적 번호)
     */
    @Column(nullable = false, unique = true)
    private String deliveryId;

    /**
     * 배송지 주소
     */
    @Column(nullable = false)
    private String address;

    /**
     * 택배사
     */
    @Column(nullable = false)
    private String carrier;

    /**
     * 배송 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    /**
     * 배송 시작 시각
     */
    @Column
    private LocalDateTime startedAt;

    /**
     * 배송 완료 시각
     */
    @Column
    private LocalDateTime completedAt;

    /**
     * 실패 사유
     */
    @Column
    private String failureReason;

    public Delivery(Long orderId, String deliveryId, String address, String carrier) {
        this.orderId = orderId;
        this.deliveryId = deliveryId;
        this.address = address;
        this.carrier = carrier;
        this.status = DeliveryStatus.PREPARING;
    }

    public enum DeliveryStatus {
        PREPARING,      // 배송 준비
        IN_TRANSIT,     // 배송 중
        DELIVERED,      // 배송 완료
        FAILED          // 배송 실패
    }

    /**
     * 배송 시작
     */
    public void start() {
        this.status = DeliveryStatus.IN_TRANSIT;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * 배송 완료
     */
    public void complete() {
        this.status = DeliveryStatus.DELIVERED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 배송 실패
     */
    public void fail(String reason) {
        this.status = DeliveryStatus.FAILED;
        this.failureReason = reason;
    }
}
