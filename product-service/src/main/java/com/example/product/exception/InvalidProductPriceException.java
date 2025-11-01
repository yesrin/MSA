package com.example.product.exception;

import com.example.common.exception.ErrorCode;
import com.example.common.exception.InvalidValueException;

import java.math.BigDecimal;

/**
 * 유효하지 않은 상품 가격일 때 발생하는 예외
 */
public class InvalidProductPriceException extends InvalidValueException {

    public InvalidProductPriceException(BigDecimal price) {
        super(ErrorCode.INVALID_PRODUCT_PRICE,
              "가격은 0보다 커야 합니다. 입력된 가격: " + price);
    }

    public InvalidProductPriceException(String message) {
        super(ErrorCode.INVALID_PRODUCT_PRICE, message);
    }
}
