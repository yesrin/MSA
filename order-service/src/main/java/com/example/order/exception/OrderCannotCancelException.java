package com.example.order.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;

/**
 * 취소할 수 없는 주문 상태일 때 발생하는 예외
 * (예: 이미 배송이 시작된 경우)
 */
public class OrderCannotCancelException extends BusinessException {

    public OrderCannotCancelException(Long orderId, String currentStatus) {
        super(ErrorCode.ORDER_CANNOT_CANCEL,
              "취소할 수 없는 주문 상태입니다. 주문 ID: " + orderId + ", 현재 상태: " + currentStatus);
    }

    public OrderCannotCancelException(String message) {
        super(ErrorCode.ORDER_CANNOT_CANCEL, message);
    }
}
