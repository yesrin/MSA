package com.example.common.exception;

/**
 * 잘못된 값이 입력되었을 때 발생하는 예외
 * 400 Bad Request 응답
 */
public class InvalidValueException extends BusinessException {

    public InvalidValueException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidValueException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
