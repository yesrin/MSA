package com.example.payment.controller;

import com.example.payment.entity.Payment;
import com.example.payment.service.PaymentService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 API 컨트롤러
 * 테스트를 위한 REST API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 처리 (기본 PG사 사용)
     */
    @PostMapping
    public ResponseEntity<PaymentResult> processPayment(@RequestBody PaymentProcessRequest request) {
        log.info("결제 요청 API 호출 - orderId: {}, amount: {}", request.getOrderId(), request.getAmount());

        Payment payment = paymentService.processPayment(request.getOrderId(), request.getAmount());

        if (payment != null) {
            return ResponseEntity.ok(new PaymentResult(true, payment.getPaymentId(), "결제 성공", null));
        } else {
            return ResponseEntity.ok(new PaymentResult(false, null, "결제 실패", null));
        }
    }

    /**
     * 결제 처리 (PG사 선택)
     */
    @PostMapping("/with-pg")
    public ResponseEntity<PaymentResult> processPaymentWithPg(@RequestBody PaymentWithPgRequest request) {
        log.info("결제 요청 API 호출 - orderId: {}, amount: {}, pgType: {}",
                request.getOrderId(), request.getAmount(), request.getPgType());

        Payment payment = paymentService.processPayment(
                request.getOrderId(),
                request.getAmount(),
                request.getPgType()
        );

        if (payment != null) {
            return ResponseEntity.ok(new PaymentResult(true, payment.getPaymentId(), "결제 성공", request.getPgType()));
        } else {
            return ResponseEntity.ok(new PaymentResult(false, null, "결제 실패", request.getPgType()));
        }
    }

    /**
     * 결제 취소
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelPayment(@PathVariable Long orderId) {
        log.info("결제 취소 API 호출 - orderId: {}", orderId);
        paymentService.cancelPayment(orderId);
        return ResponseEntity.ok("결제 취소 요청이 처리되었습니다.");
    }

    @Getter
    @AllArgsConstructor
    public static class PaymentProcessRequest {
        private Long orderId;
        private Integer amount;
    }

    @Getter
    @AllArgsConstructor
    public static class PaymentWithPgRequest {
        private Long orderId;
        private Integer amount;
        private String pgType;  // TOSS_PAYMENTS, KAKAO_PAY, NAVER_PAY
    }

    @Getter
    @AllArgsConstructor
    public static class PaymentResult {
        private boolean success;
        private String paymentId;
        private String message;
        private String pgType;
    }
}
