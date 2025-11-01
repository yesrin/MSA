package com.example.common.exception;

/**
 * Entity를 찾을 수 없을 때 발생하는 예외
 * 404 Not Found 응답
 */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EntityNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
