package com.example.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * PG 설정을 관리하는 Configuration 클래스
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "payment.gateway")
public class PaymentGatewayConfig {

    private String defaultGateway;
    private TossConfig toss;
    private KakaoConfig kakao;
    private NaverConfig naver;

    @Getter
    @Setter
    public static class TossConfig {
        private String secretKey;
        private String apiUrl;
    }

    @Getter
    @Setter
    public static class KakaoConfig {
        private String adminKey;
        private String apiUrl;
    }

    @Getter
    @Setter
    public static class NaverConfig {
        private String clientId;
        private String clientSecret;
        private String apiUrl;
    }
}
