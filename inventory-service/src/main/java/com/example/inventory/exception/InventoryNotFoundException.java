package com.example.inventory.exception;

import com.example.common.exception.EntityNotFoundException;
import com.example.common.exception.ErrorCode;

/**
 * 재고 정보를 찾을 수 없을 때 발생하는 예외
 */
public class InventoryNotFoundException extends EntityNotFoundException {

    public InventoryNotFoundException(Long productId) {
        super(ErrorCode.INVENTORY_NOT_FOUND, "상품 재고를 찾을 수 없습니다. 상품 ID: " + productId);
    }

    public InventoryNotFoundException(String message) {
        super(ErrorCode.INVENTORY_NOT_FOUND, message);
    }
}
