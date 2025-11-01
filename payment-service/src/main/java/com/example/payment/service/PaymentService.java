package com.example.payment.service;

import com.example.payment.config.PaymentGatewayConfig;
import com.example.payment.exception.PaymentNotFoundException;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.entity.Payment;
import com.example.payment.factory.PaymentGatewayFactory;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.strategy.PaymentGatewayStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory gatewayFactory;
    private final PaymentGatewayConfig gatewayConfig;

    /**
     * 결제 처리 (전략 패턴 적용)
     * @return 결제 성공 시 Payment 객체, 실패 시 null
     */
    @Transactional
    public Payment processPayment(Long orderId, Integer amount) {
        return processPayment(orderId, amount, null);
    }

    /**
     * 결제 처리 (PG사 선택 가능)
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @param pgType PG사 타입 (null이면 기본 PG사 사용)
     * @return 결제 성공 시 Payment 객체, 실패 시 null
     */
    @Transactional
    public Payment processPayment(Long orderId, Integer amount, String pgType) {
        log.info("[Payment Service] 결제 처리 요청 - orderId: {}, amount: {}, pgType: {}",
                orderId, amount, pgType);

        // PG 전략 선택
        PaymentGatewayStrategy strategy = (pgType != null)
            ? gatewayFactory.getStrategy(pgType)
            : gatewayFactory.getStrategy(gatewayConfig.getDefaultGateway());

        // PG를 통한 결제 처리
        PaymentRequest request = new PaymentRequest(orderId, amount, "CARD", "Customer", "customer@example.com");
        PaymentResponse response = strategy.processPayment(request);

        if (response.isSuccess()) {
            Payment payment = new Payment(orderId, response.getPaymentId(), amount, "CARD");
            paymentRepository.save(payment);

            log.info("✅ [Payment Service] 결제 성공 - orderId: {}, paymentId: {}, PG: {}",
                    orderId, response.getPaymentId(), response.getPgType());
            return payment;
        } else {
            log.warn("⚠️ [Payment Service] 결제 실패 - orderId: {}, PG: {}, message: {}",
                    orderId, response.getPgType(), response.getMessage());
            return null;
        }
    }

    /**
     * 결제 취소 (보상 트랜잭션)
     */
    @Transactional
    public void cancelPayment(Long orderId) {
        log.info("🔄 [Payment Service] 결제 취소 (보상 트랜잭션) - orderId: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));

        // PG사에 취소 요청
        PaymentGatewayStrategy strategy = gatewayFactory.getStrategy(gatewayConfig.getDefaultGateway());
        PaymentResponse response = strategy.cancelPayment(payment.getPaymentId());

        if (response.isSuccess()) {
            payment.cancel();
            paymentRepository.save(payment);

            log.info("✅ [Payment Service] 결제 취소 완료 - orderId: {}, paymentId: {}, PG: {}",
                    orderId, payment.getPaymentId(), response.getPgType());
        } else {
            log.error("❌ [Payment Service] 결제 취소 실패 - orderId: {}, message: {}",
                    orderId, response.getMessage());
        }
    }
}
