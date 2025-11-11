package com.example.inventory.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;

/**
 * 재고가 부족할 때 발생하는 예외
 */
public class InsufficientInventoryException extends BusinessException {

    public InsufficientInventoryException(Long productId, Integer requested, Integer available) {
        super(ErrorCode.INVENTORY_NOT_ENOUGH,
              String.format("재고가 부족합니다. 상품 ID: %d, 요청: %d, 현재: %d",
                           productId, requested, available));
    }

    public InsufficientInventoryException(String message) {
        super(ErrorCode.INVENTORY_NOT_ENOUGH, message);
    }
}
