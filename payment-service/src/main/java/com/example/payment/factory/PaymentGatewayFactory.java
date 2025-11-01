package com.example.payment.factory;

import com.example.payment.exception.UnsupportedPaymentGatewayException;
import com.example.payment.strategy.PaymentGatewayStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PG 전략을 선택하는 Factory 클래스
 * 런타임에 동적으로 PG사를 선택 가능
 */
@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory {

    private final List<PaymentGatewayStrategy> strategies;
    private final Map<String, PaymentGatewayStrategy> strategyMap = new HashMap<>();

    /**
     * 생성자 주입 시 모든 전략을 Map으로 관리
     */
    @PostConstruct
    public void init() {
        for (PaymentGatewayStrategy strategy : strategies) {
            strategyMap.put(strategy.getGatewayType(), strategy);
        }
    }

    /**
     * PG 타입에 맞는 전략 반환
     * @param gatewayType PG 타입 (TOSS_PAYMENTS, KAKAO_PAY, NAVER_PAY)
     * @return 해당하는 전략 구현체
     */
    public PaymentGatewayStrategy getStrategy(String gatewayType) {
        PaymentGatewayStrategy strategy = strategyMap.get(gatewayType);
        if (strategy == null) {
            throw new UnsupportedPaymentGatewayException(gatewayType);
        }
        return strategy;
    }

    /**
     * 기본 전략 반환 (Toss Payments)
     */
    public PaymentGatewayStrategy getDefaultStrategy() {
        return getStrategy("TOSS_PAYMENTS");
    }
}
