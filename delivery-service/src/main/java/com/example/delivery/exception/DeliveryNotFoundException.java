package com.example.delivery.exception;

import com.example.common.exception.EntityNotFoundException;
import com.example.common.exception.ErrorCode;

/**
 * 배송 정보를 찾을 수 없을 때 발생하는 예외
 */
public class DeliveryNotFoundException extends EntityNotFoundException {

    public DeliveryNotFoundException(Long deliveryId) {
        super(ErrorCode.DELIVERY_NOT_FOUND, "배송 정보를 찾을 수 없습니다. 배송 ID: " + deliveryId);
    }

    public DeliveryNotFoundException(String message) {
        super(ErrorCode.DELIVERY_NOT_FOUND, message);
    }
}
