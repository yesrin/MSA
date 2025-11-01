package com.example.payment.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;

/**
 * 결제 처리에 실패했을 때 발생하는 예외
 */
public class PaymentFailedException extends BusinessException {

    public PaymentFailedException(String message) {
        super(ErrorCode.PAYMENT_FAILED, message);
    }

    public PaymentFailedException(Long orderId, String reason) {
        super(ErrorCode.PAYMENT_FAILED,
              "결제 처리에 실패했습니다. 주문 ID: " + orderId + ", 사유: " + reason);
    }
}
