package com.example.product.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;

/**
 * 이미 비활성화된 상품을 다시 비활성화하려 할 때 발생하는 예외
 */
public class ProductAlreadyDeactivatedException extends BusinessException {

    public ProductAlreadyDeactivatedException(Long productId) {
        super(ErrorCode.PRODUCT_ALREADY_DEACTIVATED,
              "이미 비활성화된 상품입니다: " + productId);
    }
}
