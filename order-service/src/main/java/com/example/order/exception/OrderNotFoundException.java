package com.example.order.exception;

import com.example.common.exception.EntityNotFoundException;
import com.example.common.exception.ErrorCode;

/**
 * 주문을 찾을 수 없을 때 발생하는 예외
 */
public class OrderNotFoundException extends EntityNotFoundException {

    public OrderNotFoundException(Long orderId) {
        super(ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다: " + orderId);
    }

    public OrderNotFoundException(String message) {
        super(ErrorCode.ORDER_NOT_FOUND, message);
    }
}
