package com.example.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 * 각 서비스에서 발생할 수 있는 모든 에러를 중앙 집중식으로 관리
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ===== Common (1000번대) =====
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "허용되지 않은 HTTP 메서드입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근 권한이 없습니다."),

    // ===== Product (2000번대) =====
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다."),
    PRODUCT_ALREADY_DEACTIVATED(HttpStatus.BAD_REQUEST, "P002", "이미 비활성화된 상품입니다."),
    PRODUCT_STOCK_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "P003", "재고가 부족합니다."),
    INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST, "P004", "유효하지 않은 상품 가격입니다."),

    // ===== Order (3000번대) =====
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다."),
    ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "O002", "이미 취소된 주문입니다."),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "O003", "취소할 수 없는 주문 상태입니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "O004", "주문 항목을 찾을 수 없습니다."),

    // ===== Payment (4000번대) =====
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PAY002", "결제 처리에 실패했습니다."),
    PAYMENT_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "PAY003", "이미 취소된 결제입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAY004", "결제 금액이 일치하지 않습니다."),
    UNSUPPORTED_PAYMENT_GATEWAY(HttpStatus.BAD_REQUEST, "PAY005", "지원하지 않는 PG사입니다."),

    // ===== Inventory (5000번대) =====
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "재고 정보를 찾을 수 없습니다."),
    INVENTORY_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "I002", "재고가 부족합니다."),
    INVENTORY_ALREADY_RESERVED(HttpStatus.CONFLICT, "I003", "이미 예약된 재고입니다."),

    // ===== Delivery (6000번대) =====
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "배송 정보를 찾을 수 없습니다."),
    DELIVERY_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "D002", "이미 배송이 시작되었습니다."),
    DELIVERY_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "D003", "취소할 수 없는 배송 상태입니다."),

    // ===== Notification (7000번대) =====
    NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "N001", "알림 전송에 실패했습니다."),

    // ===== Kafka/Event (8000번대) =====
    EVENT_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E001", "이벤트 발행에 실패했습니다."),
    EVENT_CONSUME_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E002", "이벤트 처리에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
