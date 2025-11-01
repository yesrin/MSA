package com.example.delivery.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;

/**
 * 이미 배송이 시작된 경우 발생하는 예외
 */
public class DeliveryAlreadyStartedException extends BusinessException {

    public DeliveryAlreadyStartedException(String deliveryId) {
        super(ErrorCode.DELIVERY_ALREADY_STARTED,
              "이미 배송이 시작되었습니다. 배송 ID: " + deliveryId);
    }
}
