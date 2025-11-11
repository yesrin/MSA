package com.example.order.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;

/**
 * 이미 취소된 주문을 다시 취소하려 할 때 발생하는 예외
 */
public class OrderAlreadyCancelledException extends BusinessException {

    public OrderAlreadyCancelledException(Long orderId) {
        super(ErrorCode.ORDER_ALREADY_CANCELLED,
              "이미 취소된 주문입니다: " + orderId);
    }
}
