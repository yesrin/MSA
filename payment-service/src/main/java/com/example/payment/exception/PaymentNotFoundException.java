package com.example.payment.exception;

import com.example.common.exception.EntityNotFoundException;
import com.example.common.exception.ErrorCode;

/**
 * 결제 정보를 찾을 수 없을 때 발생하는 예외
 */
public class PaymentNotFoundException extends EntityNotFoundException {

    public PaymentNotFoundException(Long orderId) {
        super(ErrorCode.PAYMENT_NOT_FOUND, "결제 정보를 찾을 수 없습니다. 주문 ID: " + orderId);
    }

    public PaymentNotFoundException(String message) {
        super(ErrorCode.PAYMENT_NOT_FOUND, message);
    }
}
