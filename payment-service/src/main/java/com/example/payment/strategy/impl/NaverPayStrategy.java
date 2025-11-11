package com.example.payment.strategy.impl;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.strategy.PaymentGatewayStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 네이버페이 PG 전략 구현체
 * 실제로는 네이버페이 API를 호출하지만, 현재는 시뮬레이션
 */
@Slf4j
@Component
public class NaverPayStrategy implements PaymentGatewayStrategy {

    private static final String GATEWAY_TYPE = "NAVER_PAY";

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("[{}] 결제 요청 - orderId: {}, amount: {}",
                GATEWAY_TYPE, request.getOrderId(), request.getAmount());

        // 실제로는 Naver Pay API 호출
        // POST https://dev.apis.naver.com/naverpay-partner/naverpay/payments/v2.2/apply/payment

        boolean success = simulatePayment();

        if (success) {
            String pgTransactionId = "naver_" + UUID.randomUUID().toString();
            log.info("[{}] 결제 성공 - transactionId: {}", GATEWAY_TYPE, pgTransactionId);

            return PaymentResponse.builder()
                    .success(true)
                    .paymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8))
                    .pgTransactionId(pgTransactionId)
                    .message("네이버페이 결제 성공")
                    .pgType(GATEWAY_TYPE)
                    .build();
        } else {
            log.warn("[{}] 결제 실패", GATEWAY_TYPE);
            return PaymentResponse.builder()
                    .success(false)
                    .message("네이버페이 결제 실패")
                    .pgType(GATEWAY_TYPE)
                    .build();
        }
    }

    @Override
    public PaymentResponse cancelPayment(String paymentId) {
        log.info("[{}] 결제 취소 요청 - paymentId: {}", GATEWAY_TYPE, paymentId);

        // 실제로는 Naver Pay 취소 API 호출

        return PaymentResponse.builder()
                .success(true)
                .paymentId(paymentId)
                .message("네이버페이 결제 취소 완료")
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

        // 실제로는 Naver Pay 조회 API 호출

        return PaymentResponse.builder()
                .success(true)
                .paymentId(paymentId)
                .message("결제 완료")
                .pgType(GATEWAY_TYPE)
                .build();
    }

    /**
     * 결제 시뮬레이션 (88% 성공률)
     */
    private boolean simulatePayment() {
        return Math.random() > 0.12;
    }
}
