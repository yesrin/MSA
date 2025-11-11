package com.example.payment.strategy;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;

/**
 * PG 전략 인터페이스
 * 다양한 PG사(토스페이먼츠, 카카오페이, 네이버페이 등)를 추상화
 */
public interface PaymentGatewayStrategy {

    /**
     * 결제 처리
     */
    PaymentResponse processPayment(PaymentRequest request);

    /**
     * 결제 취소
     */
    PaymentResponse cancelPayment(String paymentId);

    /**
     * PG사 타입 반환
     */
    String getGatewayType();

    /**
     * 결제 상태 조회
     */
    PaymentResponse getPaymentStatus(String paymentId);
}
