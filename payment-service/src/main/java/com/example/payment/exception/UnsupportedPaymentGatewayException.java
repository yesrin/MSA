package com.example.payment.exception;

import com.example.common.exception.ErrorCode;
import com.example.common.exception.InvalidValueException;

/**
 * 지원하지 않는 PG사를 요청했을 때 발생하는 예외
 */
public class UnsupportedPaymentGatewayException extends InvalidValueException {

    public UnsupportedPaymentGatewayException(String gatewayType) {
        super(ErrorCode.UNSUPPORTED_PAYMENT_GATEWAY,
              "지원하지 않는 PG 타입입니다: " + gatewayType);
    }
}
