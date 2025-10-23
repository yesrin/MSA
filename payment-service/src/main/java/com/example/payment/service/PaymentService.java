package com.example.payment.service;

import com.example.payment.entity.Payment;
import com.example.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 결제 처리
     * @return 결제 성공 시 Payment 객체, 실패 시 null
     */
    @Transactional
    public Payment processPayment(Long orderId, Integer amount) {
        log.info("[Payment Service] 결제 처리 요청 - orderId: {}, amount: {}",
                orderId, amount);

        // 실제로는 외부 PG사 API 호출 (토스페이먼츠, NHN KCP 등)
        // 여기서는 간단히 10% 확률로 실패하도록 시뮬레이션
        boolean success = simulatePayment();

        if (success) {
            String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
            Payment payment = new Payment(orderId, paymentId, amount, "CARD");
            paymentRepository.save(payment);

            log.info("✅ [Payment Service] 결제 성공 - orderId: {}, paymentId: {}",
                    orderId, paymentId);
            return payment;
        } else {
            log.warn("⚠️ [Payment Service] 결제 실패 - orderId: {}, 잔액 부족", orderId);
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
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + orderId));

        payment.cancel();
        paymentRepository.save(payment);

        log.info("✅ [Payment Service] 결제 취소 완료 - orderId: {}, paymentId: {}",
                orderId, payment.getPaymentId());
    }

    /**
     * 결제 시뮬레이션 (90% 성공, 10% 실패)
     */
    private boolean simulatePayment() {
        return Math.random() > 0.1; // 10% 확률로 실패
    }
}
