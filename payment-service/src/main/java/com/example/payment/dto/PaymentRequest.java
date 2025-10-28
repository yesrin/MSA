package com.example.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PG 결제 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private Integer amount;
    private String paymentMethod;
    private String customerName;
    private String customerEmail;
}
