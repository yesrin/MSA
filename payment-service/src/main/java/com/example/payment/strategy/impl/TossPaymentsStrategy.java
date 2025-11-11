package com.example.payment.strategy.impl;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.strategy.PaymentGatewayStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 토스페이먼츠 PG 전략 구현체
 * 실제로는 토스페이먼츠 API를 호출하지만, 현재는 시뮬레이션
 */
@Slf4j
@Component
public class TossPaymentsStrategy implements PaymentGatewayStrategy {

    private static final String GATEWAY_TYPE = "TOSS_PAYMENTS";

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("[{}] 결제 요청 - orderId: {}, amount: {}",
                GATEWAY_TYPE, request.getOrderId(), request.getAmount());

        // 실제로는 Toss Payments API 호출
        // POST https://api.tosspayments.com/v1/payments
        // Authorization: Basic {SecretKey}

        boolean success = simulatePayment();

        if (success) {
            String pgTransactionId = "toss_" + UUID.randomUUID().toString();
            log.info("[{}] 결제 성공 - transactionId: {}", GATEWAY_TYPE, pgTransactionId);

            return PaymentResponse.builder()
                    .success(true)
                    .paymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8))
                    .pgTransactionId(pgTransactionId)
                    .message("토스페이먼츠 결제 성공")
                    .pgType(GATEWAY_TYPE)
                    .build();
        } else {
            log.warn("[{}] 결제 실패", GATEWAY_TYPE);
            return PaymentResponse.builder()
                    .success(false)
                    .message("토스페이먼츠 결제 실패 - 잔액 부족")
                    .pgType(GATEWAY_TYPE)
                    .build();
        }
    }

    @Override
    public PaymentResponse cancelPayment(String paymentId) {
        log.info("[{}] 결제 취소 요청 - paymentId: {}", GATEWAY_TYPE, paymentId);

        // 실제로는 Toss Payments 취소 API 호출
        // POST https://api.tosspayments.com/v1/payments/{paymentKey}/cancel

        return PaymentResponse.builder()
                .success(true)
                .paymentId(paymentId)
                .message("토스페이먼츠 결제 취소 완료")
                .pgType(GATEWAY_TYPE)
                .build();
    }

    @Override
    public String getGatewayType() {
        return GATEWAY_TYPE;
    }

    @Override
    public PaymentResponse getPaymentStatus(String paymentId) {
        log.info("[{}] 결제 상태 조회 - paymentId: {}", GATEWAY_TYPE, paymentId);

        // 실제로는 Toss Payments 조회 API 호출
        // GET https://api.tosspayments.com/v1/payments/{paymentKey}

        return PaymentResponse.builder()
                .success(true)
                .paymentId(paymentId)
                .message("결제 완료")
                .pgType(GATEWAY_TYPE)
                .build();
    }

    /**
     * 결제 시뮬레이션 (90% 성공률)
     */
    private boolean simulatePayment() {
        return Math.random() > 0.1;
    }
}
